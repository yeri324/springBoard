package com.myspring.pro30.board.controller;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.myspring.pro30.board.service.BoardService;
import com.myspring.pro30.board.vo.ArticleVO;
import com.myspring.pro30.board.vo.ImageVO;
import com.myspring.pro30.member.vo.MemberVO;


@Controller("boardController")
public class BoardControllerImpl implements BoardController {
	private static final String ARTICLE_IMAGE_REPO = "C:\\board\\article_image";
	@Autowired
	private BoardService boardService;
	@Autowired
	private ArticleVO articleVO;

	@Override
	@RequestMapping(value = "/board/listArticles.do", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView listArticles(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String _section=request.getParameter("section");
		String _pageNum=request.getParameter("pageNum");
		int section = Integer.parseInt(((_section==null)? "1":_section) );
		int pageNum = Integer.parseInt(((_pageNum==null)? "1":_pageNum));
		Map pagingMap=new HashMap();
		pagingMap.put("section", section);
		pagingMap.put("pageNum", pageNum);
		Map articlesMap = boardService.listArticles(pagingMap);

		articlesMap.put("section", section);
		articlesMap.put("pageNum", pageNum);
		
		String viewName = (String) request.getAttribute("viewName");
		
		ModelAndView mav = new ModelAndView(viewName);
		mav.addObject("articlesMap", articlesMap);
		return mav;

	}


	// ���� �̹��� �����ֱ�
	@RequestMapping(value = "/board/viewArticle.do", method = RequestMethod.GET)
	public ModelAndView viewArticle(@RequestParam("articleNO") int articleNO, 
												@RequestParam(value="removeCompleted", required=false) String removeCompleted,
												HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewName = (String) request.getAttribute("viewName");
		Map articleMap = boardService.viewArticle(articleNO);
		articleMap.put("removeCompleted", removeCompleted );
		ModelAndView mav = new ModelAndView();
		mav.setViewName(viewName);
		mav.addObject("articleMap", articleMap);
		return mav;
	}


	@Override
	@RequestMapping(value = "/board/removeArticle.do", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity removeArticle(@RequestParam("articleNO") int articleNO, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		response.setContentType("text/html; charset=UTF-8");
		String message;
		ResponseEntity resEnt = null;
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/html; charset=utf-8");
		try {
			boardService.removeArticle(articleNO);
			File destDir = new File(ARTICLE_IMAGE_REPO + "\\" + articleNO);
			FileUtils.deleteDirectory(destDir);

			message = "<script>";
			message += " alert('���� �����߽��ϴ�.');";
			message += " location.href='" + request.getContextPath() + "/board/listArticles.do';";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);

		} catch (Exception e) {
			message = "<script>";
			message += " alert('�۾��� ������ �߻��߽��ϴ�.�ٽ� �õ��� �ּ���.');";
			message += " location.href='" + request.getContextPath() + "/board/listArticles.do';";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
			e.printStackTrace();
		}
		return resEnt;
	}

	// ���� �̹��� �� �߰��ϱ�
	@Override
	@RequestMapping(value = "/board/addNewArticle.do", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity addNewArticle(MultipartHttpServletRequest multipartRequest, HttpServletResponse response)
			throws Exception {
		multipartRequest.setCharacterEncoding("utf-8");
		String imageFileName = null;

		Map articleMap = new HashMap();
		Enumeration enu = multipartRequest.getParameterNames();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();
			String value = multipartRequest.getParameter(name);
			articleMap.put(name, value);
		}

		// �α��� �� ���ǿ� ����� ȸ�� �������� �۾��� ���̵� ���ͼ� Map�� �����մϴ�.
		HttpSession session = multipartRequest.getSession();
		MemberVO memberVO = (MemberVO) session.getAttribute("member");
		String id = memberVO.getId();
		articleMap.put("id", id);
		String parentNO = (String)session.getAttribute("parentNO")  ;
		articleMap.put("parentNO" , (parentNO == null ? 0 : parentNO));

		List<String> fileList = upload(multipartRequest);
		List<ImageVO> imageFileList = new ArrayList<ImageVO>();
		if (fileList != null && fileList.size() != 0) {
			for (String fileName : fileList) {
				ImageVO imageVO = new ImageVO();
				imageVO.setImageFileName(fileName);
				imageFileList.add(imageVO);
			}
			articleMap.put("imageFileList", imageFileList);
		}

		String message;
		ResponseEntity resEnt = null;
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/html; charset=utf-8");
		try {
			int articleNO = boardService.addNewArticle(articleMap);
			if (imageFileList != null && imageFileList.size() != 0) {
				for (ImageVO imageVO : imageFileList) {
					imageFileName = imageVO.getImageFileName();
					File srcFile = new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + imageFileName);
					File destDir = new File(ARTICLE_IMAGE_REPO + "\\" + articleNO);
					// destDir.mkdirs();
					FileUtils.moveFileToDirectory(srcFile, destDir, true);
				}
			}

			message = "<script>";
			message += " alert('������ �߰��߽��ϴ�.');";
			message += " location.href='" + multipartRequest.getContextPath() + "/board/viewArticle.do?articleNO=" + articleMap.get("articleNO") + "';" ;
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);

		} catch (Exception e) {
			if (imageFileList != null && imageFileList.size() != 0) {
				for (ImageVO imageVO : imageFileList) {
					imageFileName = imageVO.getImageFileName();
					File srcFile = new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + imageFileName);
					srcFile.delete();
				}
			}

			message = " <script>";
			message += " alert('������ �߻��߽��ϴ�. �ٽ� �õ��� �ּ���');');";
			message += " location.href='" + multipartRequest.getContextPath() + "/board/articleForm.do'; ";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
			e.printStackTrace();
		}
		return resEnt;
	}

	// ���� �̹��� ���� ���
	@RequestMapping(value = "/board/modArticle.do", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity modArticle(MultipartHttpServletRequest multipartRequest, HttpServletResponse response)
			throws Exception {
		multipartRequest.setCharacterEncoding("utf-8");
		Map<String, Object> articleMap = new HashMap<String, Object>();
		Enumeration enu = multipartRequest.getParameterNames();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();

			if (name.equals("imageFileNO")) {
				String[] values = multipartRequest.getParameterValues(name);
				articleMap.put(name, values);
			} else if (name.equals("oldFileName")) {
				String[] values = multipartRequest.getParameterValues(name);
				articleMap.put(name, values);
			} else {
				String value = multipartRequest.getParameter(name);
				articleMap.put(name, value);
			}

		}

		List<String> fileList = uploadModImageFile(multipartRequest); //������ �̹��� ������ ���ε��Ѵ�.

		int added_img_num = Integer.parseInt((String) articleMap.get("added_img_num"));
		int pre_img_num = Integer.parseInt((String) articleMap.get("pre_img_num"));
		List<ImageVO> imageFileList = new ArrayList<ImageVO>();
		List<ImageVO> modAddimageFileList = new ArrayList<ImageVO>();

		if (fileList != null && fileList.size() != 0) {
			String[] imageFileNO = (String[]) articleMap.get("imageFileNO");
			for (int i = 0; i < added_img_num; i++) {
				String fileName = fileList.get(i);
				ImageVO imageVO = new ImageVO();
				if (i < pre_img_num) {
					imageVO.setImageFileName(fileName);
					imageVO.setImageFileNO(Integer.parseInt(imageFileNO[i]));
					imageFileList.add(imageVO);
					articleMap.put("imageFileList", imageFileList);
				} else {
					imageVO.setImageFileName(fileName);
					modAddimageFileList.add(imageVO);
					articleMap.put("modAddimageFileList", modAddimageFileList);
				}
			}
		}


		String articleNO = (String) articleMap.get("articleNO");
		String message;
		ResponseEntity resEnt = null;
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/html; charset=utf-8");
		try {
			boardService.modArticle(articleMap);
			if (fileList != null && fileList.size() != 0) { // ������ ���ϵ��� ���ʴ�� ���ε��Ѵ�.
				for (int i = 0; i < fileList.size(); i++) {
					String fileName = fileList.get(i);
					if (i  < pre_img_num ) {
						if (fileName != null) {
							File srcFile = new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + fileName);
							File destDir = new File(ARTICLE_IMAGE_REPO + "\\" + articleNO);
							FileUtils.moveFileToDirectory(srcFile, destDir, true);

							String[] oldName = (String[]) articleMap.get("oldFileName");
							String oldFileName = oldName[i];

							File oldFile = new File(ARTICLE_IMAGE_REPO + "\\" + articleNO + "\\" + oldFileName);
							oldFile.delete();
						}
					}else {
						
						if (fileName != null) {
							File srcFile = new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + fileName);
							File destDir = new File(ARTICLE_IMAGE_REPO + "\\" + articleNO);
							FileUtils.moveFileToDirectory(srcFile, destDir, true);
						}
	
					}
				}
			}

			message = "<script>";
			message += " alert('���� �����߽��ϴ�.');";
			message += " location.href='" + multipartRequest.getContextPath() + "/board/viewArticle.do?articleNO="
					+ articleNO + "';";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
		} catch (Exception e) {
			
			if (fileList != null && fileList.size() != 0) { // ���� �߻� �� temp ������ ���ε�� �̹��� ���ϵ��� �����Ѵ�.
				for (int i = 0; i < fileList.size(); i++) {
					File srcFile = new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + fileList.get(i));
					srcFile.delete();
				}

				e.printStackTrace();
			}

			message = "<script>";
			message += " alert('������ �߻��߽��ϴ�.�ٽ� �������ּ���');";
			message += " location.href='" + multipartRequest.getContextPath() + "/board/viewArticle.do?articleNO="
					+ articleNO + "';";
			message += " </script>";
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
		}
		return resEnt;
	}

	// �����ϱ⿡�� �̹��� ���� ���
	@RequestMapping(value = "/board/removeModImage.do", method = RequestMethod.POST)
	@ResponseBody
	public void removeModImage(HttpServletRequest request, HttpServletResponse response) throws Exception {

		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html; charset=utf-8");
		PrintWriter writer = response.getWriter();

		try {
			String imageFileNO = (String) request.getParameter("imageFileNO");
			String imageFileName = (String) request.getParameter("imageFileName");
			String articleNO = (String) request.getParameter("articleNO");
			
			System.out.println("imageFileNO = " + imageFileNO);
			System.out.println("articleNO = " + articleNO);

			ImageVO imageVO = new ImageVO();
			imageVO.setArticleNO(Integer.parseInt(articleNO));
			imageVO.setImageFileNO(Integer.parseInt(imageFileNO));
			boardService.removeModImage(imageVO);
			
			File oldFile = new File(ARTICLE_IMAGE_REPO + "\\" + articleNO + "\\" + imageFileName);
			oldFile.delete();
			
			writer.print("success");
		} catch (Exception e) {
			writer.print("failed");
		}

	}

	@RequestMapping(value = "/board/*Form.do", method = {RequestMethod.GET, RequestMethod.POST})
	private ModelAndView form(@RequestParam(value="result", required=false) String result,
			@RequestParam(value="action", required=false) String action,
			@RequestParam(value="parentNO", required=false) String parentNO,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String viewName = (String) request.getAttribute("viewName");
		HttpSession session = request.getSession();
		session.setAttribute("action", action);
		if (parentNO != null) {		// ��۾���� �α��� ��
			session.setAttribute("parentNO", parentNO);		// get ���
		}
		ModelAndView mav = new ModelAndView();
		mav.addObject("result", result);
		mav.setViewName(viewName);
		return mav;
	}


	// �� �� ���� �� ���� �̹��� ���ε��ϱ�
	private List<String> upload(MultipartHttpServletRequest multipartRequest) throws Exception {
		List<String> fileList = new ArrayList<String>();
		Iterator<String> fileNames = multipartRequest.getFileNames();
		while (fileNames.hasNext()) {
			String fileName = fileNames.next();
			MultipartFile mFile = multipartRequest.getFile(fileName);
			String originalFileName = mFile.getOriginalFilename();
			if (originalFileName != "" && originalFileName != null) {
				fileList.add(originalFileName);
				File file = new File(ARTICLE_IMAGE_REPO + "\\" + fileName);
				if (mFile.getSize() != 0) { // File Null Check
					if (!file.exists()) { // ��λ� ������ �������� ���� ���
						file.getParentFile().mkdirs(); // ��ο� �ش��ϴ� ���丮���� ����
						mFile.transferTo(new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + originalFileName)); // �ӽ÷�
																													// �����
																													// multipartFile��
																													// ����
																													// ���Ϸ�
																													// ����
					}
				}
			}

		}
		return fileList;
	}

	// ���� �� ���� �̹��� ���ε��ϱ�
	private List<String> uploadModImageFile(MultipartHttpServletRequest multipartRequest) throws Exception {
		List<String> fileList = new ArrayList<String>();
		Iterator<String> fileNames = multipartRequest.getFileNames();
		while (fileNames.hasNext()) {
			String fileName = fileNames.next();
			MultipartFile mFile = multipartRequest.getFile(fileName);
			String originalFileName = mFile.getOriginalFilename();
			if (originalFileName != "" && originalFileName != null) {
				fileList.add(originalFileName);
				File file = new File(ARTICLE_IMAGE_REPO + "\\" + fileName);
				if (mFile.getSize() != 0) { // File Null Check
					if (!file.exists()) { // ��λ� ������ �������� ���� ���
						file.getParentFile().mkdirs(); // ��ο� �ش��ϴ� ���丮���� ����
						mFile.transferTo(new File(ARTICLE_IMAGE_REPO + "\\" + "temp" + "\\" + originalFileName)); // �ӽ÷�
					}
				}
			} else {
				fileList.add(null);
			}

		}
		return fileList;
	}
}
