package com.solid.check;

public class AnalyzedLine {
	private String content;
	private boolean error;
	private String comment;

	public AnalyzedLine(String content, boolean error, String comment) {
		this.content = content;
		this.error = error;
		this.comment = comment;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
