package com.myspring.pro30.board.dao;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

import com.myspring.pro30.board.vo.ArticleVO;
import com.myspring.pro30.board.vo.ImageVO;

public interface BoardDAO {
	//public List<ArticleVO> selectAllArticlesList() throws DataAccessException;
	
	public List selectAllArticlesList(Map articleMap) throws DataAccessException;
	public int selectTotArticles() throws DataAccessException;
	

	public int insertNewArticle(Map articleMap) throws DataAccessException;
	public void insertNewImage(Map articleMap) throws DataAccessException;
	
	
	public ArticleVO selectArticle(int articleNO) throws DataAccessException;
	public void updateArticle(Map articleMap) throws DataAccessException;
	public void updateImageFile(Map articleMap) throws DataAccessException;
	
	
	public void deleteArticle(int articleNO) throws DataAccessException;
	public List selectImageFileList(int articleNO) throws DataAccessException;
	
	
	public void deleteModImage(ImageVO imageVO) throws DataAccessException;
	
	
	public void insertModNewImage(Map articleMap) throws DataAccessException;

}
