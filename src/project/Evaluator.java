package project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Evaluator for HW1.
 * 
 * @author fdiaz
 * @author congyu
 */
class Evaluator {
  public static class DocumentRelevances {
    private Map<Integer, Double> relevances = new HashMap<Integer, Double>();
    
    public DocumentRelevances() { }
    
    public void addDocument(int docid, String grade) {
      relevances.put(docid, convertToRelevance(grade));
    }
    
    public boolean hasRelevanceForDoc(int docid) {
      return relevances.containsKey(docid);
    }
    
    public double getRelevanceForDoc(int docid) {
      return relevances.get(docid);
    }

    public double getRelevance;

    public int countRelevanceDoc(){
      int count = 0;
      for (Map.Entry<Integer, Double> entry : relevances.entrySet())
      {
        if(entry.getValue() > 1){
          count += 1;
        }
      }
      return count;
    }
    
    private static double convertToRelevance(String grade) {
      switch(grade){
        case "Perfect" : return 10.0; 
        case "Excellent" : return 7.0; 
        case "Good" : return 5.0; 
        case "Fair" : return 1.0; 
        case "Bad" : return 0.0;
      }
      return 0.0;
    }
  }
  
  /**
   * Usage: java -cp src edu.nyu.cs.cs2580.Evaluator [labels] [ranker] [metric_id]
   */
  public static void main(String[] args) throws IOException {
    Map<String, DocumentRelevances> judgments = new HashMap<String, DocumentRelevances>();
    SearchEngine.Check(args.length >= 2, "Must provide labels and ranker at least!");
    //in this case, args[0] = data/labels.tsv
    readRelevanceJudgments(args[0], judgments);
    String dir = generateFile(args[1]);
    if(args.length == 3){
    	evaluateStdin(Integer.parseInt(args[2]), judgments, dir);
    }
    else{
    	evaluateStdin(judgments, dir);
    }
  }
  
  public static void readRelevanceJudgments(String judgeFile, Map<String, DocumentRelevances> judgements) throws IOException {
    String line = null;
    BufferedReader reader = new BufferedReader(new FileReader(judgeFile));
    while ((line = reader.readLine()) != null) {
      // Line format: query \t docid \t grade
	Scanner s = new Scanner(line).useDelimiter("\t");
      String query = s.next();
      DocumentRelevances relevances = judgements.get(query);
      // Query don't exist
      if (relevances == null) {
        relevances = new DocumentRelevances();
        judgements.put(query, relevances);
      }
      relevances.addDocument(Integer.parseInt(s.next()), s.next());
      s.close();
    }
    reader.close();
  }

  public static String generateFile(String ranker) throws IOException{
	  String dir = System.getProperty("user.dir");
	  String filedir = dir;
	  switch(ranker){
          case "fullscan" : filedir = dir + "/results/hw1.3-fullscan.tsv";break;
          case "cosine": filedir = dir + "/results/hw1.3-vsm.tsv";break;
          case "ql": filedir = dir + "/results/hw1.3-ql.tsv";break;
          case "phrase": filedir = dir + "/results/hw1.3-phrase.tsv";break;
	      case "numviews": filedir = dir + "/results/hw1.3-numviews.tsv";break;
          case "linear": filedir = dir + "/results/hw1.3-linear.tsv";break;
      default:System.out.println("Request ranker is not implemented");
	  }
      
      File file = new File(filedir);
      if(!file.exists()){
          file.createNewFile();
      }
      
	  return filedir;
  }
  
  // @CS2580: implement various metrics inside this function
  public static void evaluateStdin(int metric, Map<String, DocumentRelevances> judgments, String dir) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader("./numviews.result"));
    List<Integer> results = new ArrayList<Integer>();
    String line = null;
    String currentQuery="";
    while ((line = reader.readLine()) != null) {
      Scanner s = new Scanner(line).useDelimiter("\t");
      currentQuery = s.next();
      results.add(Integer.parseInt(s.next()));
      s.close();
    }
    reader.close();
    
    File file = new File(dir);
    BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
    
    if (results.size() > 0) {
    	writer.write(currentQuery + "\t");
    	switch (metric) {
        	case -1: evaluateQueryInstructor(currentQuery, results, judgments); break;
            case 0: {
            	writer.write("Precision@1="+String.format("%.2f", Precision(currentQuery, results, judgments, 1))+"\t");
            	writer.write("Precision@5="+String.format("%.2f", Precision(currentQuery, results, judgments, 5))+"\t");
            	writer.write("Precision@10="+String.format("%.2f", Precision(currentQuery, results, judgments, 10))+"\t");
            }break;//Precision at 1, 5, and 10
            case 1: {
            	writer.write("Recall@1="+String.format("%.2f",Recall(currentQuery, results, judgments, 1))+"\t");
            	writer.write("Recall@5="+String.format("%.2f",Recall(currentQuery, results, judgments, 5))+"\t");
            	writer.write("Recall@10="+String.format("%.2f",Recall(currentQuery, results, judgments, 10))+"\t");
            }break;//Recall at 1, 5, and 10
            case 2: {
            	writer.write("F(0.5)@1="+String.format("%.2f",F(currentQuery, results, judgments, 1, 0.50))+"\t");
            	writer.write("F(0.5)@5="+String.format("%.2f",F(currentQuery, results, judgments, 5, 0.50))+"\t");
            	writer.write("F(0.5)@10="+String.format("%.2f",F(currentQuery, results, judgments, 10, 0.50))+"\t");
            }break;//F0.50 at 1, 5, and 10
            case 3: {
            	double[] points = PreAndRe(currentQuery, results, judgments);
            	writer.write("Precision at recall points:");
            	for(int i=0; i<11; i++){
            		writer.write(String.format("%.2f",points[i]));
            		if(i!=10){
            			writer.write(",");
            		}
            	}
            }break;//Precision at recall points{0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0}
            case 4: {
            	writer.write("Average precision="+String.format("%.2f", MAP(currentQuery, results, judgments))+"\t");
            }break;//Average precision
            case 5: {
            	writer.write("NDCG@1="+String.format("%.2f", NDCG(currentQuery, results, judgments, 1))+"\t");
            	writer.write("NDCG@5="+String.format("%.2f", NDCG(currentQuery, results, judgments, 5))+"\t");
            	writer.write("NDCG@10="+String.format("%.2f", NDCG(currentQuery, results, judgments, 10))+"\t");
            }break;//NDCG at 1, 5, and 10 (using the gain values presented in Lecture 2)
            case 6:{
              	writer.write("Reciprocal="+String.format("%.2f", Reciprocal(currentQuery, results, judgments))+"\t");
            }break;//Reciprocal rank
            default:
            // @CS2580: add your own metric evaluations above, using function
            // names like evaluateQueryMetric0.
            System.err.println("Requested metric not implemented!");
          }
      }
    writer.write("\n");
    writer.flush();
    writer.close();
  }
  
  public static void evaluateStdin(Map<String, DocumentRelevances> judgments, String dir) throws IOException{
	  	BufferedReader reader = new BufferedReader(new FileReader("./test5.tsv"));
	    List<Integer> results = new ArrayList<Integer>();
	    String line = null;
	    String currentQuery="";
	    while ((line = reader.readLine()) != null) {
                Scanner s = new Scanner(line).useDelimiter("\t");
                currentQuery = s.next();
                results.add(Integer.parseInt(s.next()));
                s.close();
	    }
	    reader.close();
	    
	    File file = new File(dir);
	    BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
	    
	    if (results.size() > 0) {
	    	writer.write(currentQuery + "\t");
	    	evaluateQueryInstructor(currentQuery, results, judgments);
	    	writer.write("Precision@1="+String.format("%.2f", Precision(currentQuery, results, judgments, 1))+"\t");
        	writer.write("Precision@5="+String.format("%.2f", Precision(currentQuery, results, judgments, 5))+"\t");
        	writer.write("Precision@10="+String.format("%.2f", Precision(currentQuery, results, judgments, 10))+"\t");
        	writer.write("Recall@1="+String.format("%.2f",Recall(currentQuery, results, judgments, 1))+"\t");
        	writer.write("Recall@5="+String.format("%.2f",Recall(currentQuery, results, judgments, 5))+"\t");
        	writer.write("Recall@10="+String.format("%.2f",Recall(currentQuery, results, judgments, 10))+"\t");
        	writer.write("F(0.5)@1="+String.format("%.2f",F(currentQuery, results, judgments, 1, 0.50))+"\t");
        	writer.write("F(0.5)@5="+String.format("%.2f",F(currentQuery, results, judgments, 5, 0.50))+"\t");
        	writer.write("F(0.5)@10="+String.format("%.2f",F(currentQuery, results, judgments, 10, 0.50))+"\t");
	        
        	writer.write("\n");
        	double[] points = PreAndRe(currentQuery, results, judgments);
        	writer.write("Precision at recall points:");
        	for(int i=0; i<11; i++){
        		writer.write(String.format("%.2f",points[i])+",");
        	}
        	writer.write("\t\n");
        	
        	writer.write("Average precision="+String.format("%.2f", MAP(currentQuery, results, judgments))+"\t");
        	
        	writer.write("NDCG@1="+String.format("%.2f", NDCG(currentQuery, results, judgments, 1))+"\t");
        	writer.write("NDCG@5="+String.format("%.2f", NDCG(currentQuery, results, judgments, 5))+"\t");
        	writer.write("NDCG@10="+String.format("%.2f", NDCG(currentQuery, results, judgments, 10))+"\t");
        	
        	writer.write("Reciprocal="+String.format("%.2f", Reciprocal(currentQuery, results, judgments))+"\t");
	    }
	    writer.write("\n");
	    writer.flush();
	    writer.close();
  }
  
  public static void evaluateQueryInstructor(String query, List<Integer> docids, Map<String, DocumentRelevances> judgments) {
    double R = 0.0;
    double N = 0.0;
    for (int docid : docids) {
      DocumentRelevances relevances = judgments.get(query);
      if (relevances == null) {
        System.out.println("Query [" + query + "] not found!");
      } else {
        if (relevances.hasRelevanceForDoc(docid)) {
          R += relevances.getRelevanceForDoc(docid);
        }
        ++N;
      }
    }
    System.out.println(query + "\t" + Double.toString(R / N));
  }

  public static double Precision(String query, List<Integer> docids, Map<String, DocumentRelevances> judgments, int location){
    int docs = 0;
    DocumentRelevances relevances = judgments.get(query);
    if (relevances == null){
      System.out.println("Query [" + query + "] not found!");
    }
    else{
      for(int i = 0; i < location; i++){
        int docid = docids.get(i);
        if(relevances.hasRelevanceForDoc(docid)){
          if(relevances.getRelevanceForDoc(docid) > 1.0){
            docs += 1;
          }
        }
      }
    }
    return docs*1.0/location;
  }

  public static double Recall(String query, List<Integer> docids, Map<String, DocumentRelevances> judgments, int location){
    int docs = 0;
    DocumentRelevances relevances = judgments.get(query);
    if (relevances == null){
      System.out.println("Query [" + query + "] not found!");
      return 0.0;
    }
    else{
      for(int i = 0; i < location; i++){
        int docid = docids.get(i);
        if(relevances.hasRelevanceForDoc(docid)){
          if(relevances.getRelevanceForDoc(docid) > 1.0){
            docs += 1;
          }
        }
      }
      return docs*1.0/relevances.countRelevanceDoc();
    }
  }

  public static double F(String query, List<Integer> docids, Map<String, DocumentRelevances> judgments, int location, double alpha){
    double precision = Precision(query, docids, judgments, location);
    double recall = Recall(query, docids, judgments, location);
    double f = 0.0;
    if(precision > 0 && recall > 0){
      f = 1/(alpha*(1/precision)+(1-alpha)*(1/recall));
    }
    else{
      if(precision == 0 && recall == 0){
        f = 0.0;
      }
      else{
        if(precision == 0){
          f = 1/((1-alpha)*(1/recall));
        }
        else{
          f = 1/(alpha*(1/precision));
        }
      }
    }
    return f;
  }

  public static double[] PreAndRe(String query, List<Integer> docids, Map<String, DocumentRelevances> judgments){
    double[] precisions = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    DocumentRelevances relevances = judgments.get(query);
    if(relevances == null){
      System.out.println("Query [" + query + "] not found!");
    }
    else{
      for(int i = 1; i <= docids.size(); i++){
    	  double tempRecall = Recall(query, docids, judgments, i);
    	  double tempPrecision = Precision(query, docids, judgments, i);
    	  int index = (int)(tempRecall*10);
    	  if(tempPrecision > precisions[index]){
    		  precisions[index] = tempPrecision;
    	  }
      }
    }
      
    int m = 0;
    for(int i = 0; i<11; i++){
    	if(precisions[i] != 0){
    		for(int j = m; j< i; j++){
    			precisions[j] = precisions[i];
    		}
    		m = i+1;
    	}
    }
    return precisions;
  }

  public static double MAP(String query, List<Integer> docids, Map<String, DocumentRelevances> judgments){
    double map = 0;
    double totalPre = 0.0;
    int retrievedDoc = 0;
    DocumentRelevances relevances = judgments.get(query);
    if(relevances == null){
      System.out.println("Query [" + query + "] not found!");
    }
    else{
      for(int i=1; i<=docids.size(); i++){
    	if(relevances.hasRelevanceForDoc(docids.get(i-1))){
    		if(relevances.getRelevanceForDoc(docids.get(i-1)) > 1.0){
    	          retrievedDoc += 1;
    	          totalPre += Precision(query, docids, judgments, i);
    	    }
    	}
      }
    }
    if(retrievedDoc > 0){
      map = totalPre/retrievedDoc;
    }
    return map;
  }

  public static double Reciprocal(String query, List<Integer> docids, Map<String, DocumentRelevances> judgments){
    double rr = 0.0;
    DocumentRelevances relevances = judgments.get(query);
    if(relevances == null){
      System.out.println("Query [" + query + "] not found!");
    }
    else{
      for(int i = 0; i<docids.size(); i++){
          int docid = docids.get(i);
        if(relevances.hasRelevanceForDoc(docid)){
            if(relevances.getRelevanceForDoc(docid) > 1.0){
                rr = 1.0/(i+1);
                break;
            }
        }
      }
    }
    return rr;
  }

  public static double DCG(String query, List<Integer> docids, Map<String, DocumentRelevances> judgments, int location){
    double dcg = 0.0;
    double grade = 0.0;
    DocumentRelevances relevances = judgments.get(query);
    if(relevances == null){
      System.out.println("Query [" + "] not found!");
    }
    else{
      for(int i=0; i<location; i++){
        if(relevances.hasRelevanceForDoc(docids.get(i))) {
          grade = relevances.getRelevanceForDoc(docids.get(i));
          if(i == 0){
        	  dcg += grade;
          }
          else{
        	  if(grade != 0.0){
        		  dcg += grade/log2(i+1);
        	  }
          }
        }
      }
    }
    return dcg;
  }

  public static double IDCG(String query, List<Integer> docids, Map<String, DocumentRelevances> judgments, int location){
    double idcg = 0.0;
    List<Double> grades = new ArrayList<Double>();
    DocumentRelevances relevances = judgments.get(query);
    if(relevances == null){
      System.out.println("Query [" + "] not found!");
    }
    else{
      for(int i = 0; i<docids.size(); i++){
        if(relevances.hasRelevanceForDoc(docids.get(i))){
          grades.add(relevances.getRelevanceForDoc(docids.get(i)));
        }
        else{
          grades.add(0.0);
        }
      }
      Collections.sort(grades);
      Collections.reverse(grades);
      for(int i=0; i<location; i++){
    	  if(i == 0){
        	  idcg += grades.get(i);
          }
          else{
        	  if(grades.get(i) != 0.0){
        		  idcg += grades.get(i)/log2(i+1);
        	  }
          }
      }
    }
    return idcg;
  }

  public static double NDCG(String query, List<Integer> docids, Map<String, DocumentRelevances> judgments, int location){
    double ndcg = 0.0;
    double dcg = DCG(query, docids, judgments, location);
    double idcg = IDCG(query, docids, judgments, location);
    if(idcg != 0){
      ndcg = dcg/idcg;
    }
    return ndcg;
  }

  public static double log2(int data){
    double answer = 0.0;
    double a = Math.log(data);
    double b = Math.log(2);
    answer = a/b;
    return answer;
  }
}
