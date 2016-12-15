package project;

public class ScoredMovie implements Comparable<ScoredMovie> {
	private Movie _movie;
	private double _score;
	
	public ScoredMovie(Movie movie, double score){
		_movie = movie;
		_score = score;
	}
	
	public String asHtmlResult(){
		StringBuffer buf = new StringBuffer();
		buf.append("<tr>");
		buf.append("<td>").append(_movie.getName()).append("</td>");
		buf.append("<td>").append(_movie.getYear()).append("</td>");
		buf.append("<td>").append(_movie.getRating()).append("</td>");
		buf.append("<td>").append(_movie.getRatingsCount()).append("</td>");
		buf.append("<td>").append(_movie.getDirector()).append("</td>");
		buf.append("<td>").append(_movie.getGenres()).append("</td>");
		buf.append("<td>").append(_movie.getDescription()).append("</td>");
		buf.append("</tr>");
		return buf.toString();
	}
	
	@Override
	public int compareTo(ScoredMovie m){
		if(this._score == m._score){
			return 0;
		}
		return (this._score > m._score) ? 1 : -1;
	}
}
