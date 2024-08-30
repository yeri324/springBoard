package com.myspring.pro30.board.service;

import java.util.Map;

import com.myspring.pro30.board.vo.ImageVO;

public interface BoardService {
	
	//public List<ArticleVO> listArticles() throws Exception;
	
	public Map listArticles(Map pagingMap) throws Exception;
	
	public int selectTotArticles() throws Exception;
	
	
	
	public int addNewArticle(Map articleMap) throws Exception;
	
	public Map viewArticle(int articleNO) throws Exception;
	
	public void modArticle(Map articleMap) throws Exception;
	
	public void removeArticle(int articleNO) throws Exception;
	
	public void removeModImage(ImageVO imageVO) throws Exception;
	
	
}
