package com.myspring.pro30.board.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public interface BoardController {
	
	public ModelAndView listArticles(HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	public ResponseEntity addNewArticle(MultipartHttpServletRequest multipartRequest,HttpServletResponse response) throws Exception;
	
	public ModelAndView viewArticle(@RequestParam("articleNO") int articleNO,
												@RequestParam(value="removeCompleted", required=false) String removeCompleted,
												HttpServletRequest request, HttpServletResponse response) throws Exception;
	public ResponseEntity  removeArticle(@RequestParam("articleNO") int articleNO,
                              HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	public void removeModImage(HttpServletRequest request, HttpServletResponse response) throws Exception;

	
	
	public ResponseEntity modArticle(MultipartHttpServletRequest multipartRequest,  HttpServletResponse response) throws Exception;
	

}
