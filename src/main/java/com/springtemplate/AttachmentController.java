package com.springtemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ModelAndView;

import com.springtemplate.attachment.dto.Attachment;
import com.springtemplate.attachment.service.AttachmentService;

@PermitAll
@Controller
public class AttachmentController {
	private Logger logger = org.slf4j.LoggerFactory
			.getLogger(AttachmentController.class);
	public static final String ADDED_ATTACHMENT_ID_LIST_KEY = "addedAttachmentIdList";
	public static final String DELETED_ATTACHMENT_ID_LIST_KEY = "deletedAttachmentIdList";
	public static final String ATTACHMENT_FILE_LIST_KEY = "attachmentFileList";

	@Autowired
	private AttachmentService attachmentService;

	@Autowired
	private CommonsMultipartResolver multipartResolver;

	@RequestMapping("/welcome")
	public ModelAndView welcome() {
		logger.info("Welcome!");
		List<Attachment> attachmentFileList = attachmentService
				.getAttachmentList( 1L );

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("attachments", attachmentFileList);
		return new ModelAndView("welcome", map);
		
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/insertattachment")
	public String insertAttachment(HttpServletRequest request,
			HttpServletResponse response,
			@ModelAttribute("fileUploadForm") Attachment attachment)
			throws Exception {
		logger.info("AttachmentController.insertAttachment");

		MultipartHttpServletRequest defRequest = null;

		try {
			MultipartFile multipartFile = null;
		

			HttpSession session = request.getSession();
			List<String> attachmentIdList = null;
			boolean isInsertSuccess = false;

			if (multipartResolver.isMultipart(request)) {
				defRequest = (MultipartHttpServletRequest) request;
				multipartFile = attachment.getUploadedFile();
				attachment.setFilename(multipartFile.getOriginalFilename());
				attachment.setMimeType(multipartFile.getContentType());
				attachment.setDescription(attachment.getDescription());

				

				attachmentIdList = (List<String>) session
						.getAttribute(ADDED_ATTACHMENT_ID_LIST_KEY);
				if (attachmentIdList == null) {
					attachmentIdList = new ArrayList<String>();
				}
			

				isInsertSuccess = attachmentService
						.insertAttachment(attachment);
			}
			if (isInsertSuccess) {
				response.getWriter().print("success");
			}
		} catch (Exception e) {
			response.getWriter().print("error");

		} finally {
			multipartResolver.cleanupMultipart(defRequest);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/doupdateattachment")
	public String doUpdateAttachment(HttpServletRequest request) {
		logger.debug("AttachmentController.doUpdateAttachment");
		HttpSession session = null;
		try {
			session = request.getSession();
			List<String> addedAttachmentIdList = (List<String>) session
					.getAttribute(ADDED_ATTACHMENT_ID_LIST_KEY);
			List<String> deletedAttachmentIdList = (List<String>) session
					.getAttribute(DELETED_ATTACHMENT_ID_LIST_KEY);

			String requestIdStr = request.getParameter("requestId");
			logger.debug("=========> requestId : " + requestIdStr);
			Long requestId = Long.parseLong(requestIdStr);
			attachmentService.updateAttachments(addedAttachmentIdList,
					deletedAttachmentIdList, requestId);

		} catch (Exception e) {

		} finally {
			session.removeAttribute(ADDED_ATTACHMENT_ID_LIST_KEY);
			session.removeAttribute(DELETED_ATTACHMENT_ID_LIST_KEY);
		}
		return "showtestscreen";
	}

	@RequestMapping("/getattachments")
	public ModelAndView getAttachments(
			@RequestParam("requestid") String requestId,
			HttpServletRequest request) {
		logger.debug("AttachmentController.getAttachments");
		List<Attachment> attachmentFileList = attachmentService
				.getAttachmentList(Long.parseLong(requestId));

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("attachments", attachmentFileList);

		return new ModelAndView("welcome", map);
	}

	@RequestMapping("/downloadattachment")
	public String downloadAttachment(HttpServletRequest request,
			HttpServletResponse response) {
		logger.debug("AttachmentController.downloadAttachment");
		String attachId = request.getParameter("selectedFileId");
		Attachment downloadObj = attachmentService.downloadAttachment(attachId);

		if (downloadObj != null) {
			String filename = downloadObj.getFilename();
			String contentType = downloadObj.getMimeType();
			logger.debug("=====>>> contentType: " + contentType);
			String header = "attachment; filename=\"" + filename + "\"";
			response.reset();
			response.setContentType(contentType);
			response.setHeader("Content-disposition", header);

			try {
				FileCopyUtils.copy(downloadObj.getFileData().getBinaryStream(),
						response.getOutputStream());
			} catch (Exception e) {

			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/deleteattachment")
	public String deleteAttachment(HttpServletRequest request,
			HttpServletResponse response) {
		logger.debug("AttachmentController.deleteAttachment");
		String deletedId = request.getParameter("selectedAttachId");
		logger.debug("deletedId : " + deletedId);
		HttpSession session = request.getSession();
		try {
			List<String> addedAttachmentIdList = (List<String>) session
					.getAttribute(ADDED_ATTACHMENT_ID_LIST_KEY);
			List<String> deletedAttachmentIdList = (List<String>) session
					.getAttribute(DELETED_ATTACHMENT_ID_LIST_KEY);

			if (deletedAttachmentIdList == null) {
				deletedAttachmentIdList = new ArrayList<String>();
			}
			int i = 0;
			if (addedAttachmentIdList != null
					&& addedAttachmentIdList.size() > 0) {
				boolean isFound = false;
				for (String id : addedAttachmentIdList) {
					if (deletedId.equals(id)) {
						addedAttachmentIdList.remove(i);
						deletedAttachmentIdList.add(deletedId);
						isFound = true;
						break;
					}
					i++;
				}
				if (!isFound) {
					deletedAttachmentIdList.add(deletedId);
				}
			} else {
				deletedAttachmentIdList.add(deletedId);
			}

			session.setAttribute(ADDED_ATTACHMENT_ID_LIST_KEY,
					addedAttachmentIdList);
			session.setAttribute(DELETED_ATTACHMENT_ID_LIST_KEY,
					deletedAttachmentIdList);
			// attachmentService.deleteAttachment(deletedId);
			response.getWriter().print("success");
		} catch (Exception e) {
			try {
				response.getWriter().print("failure");
			} catch (Exception e2) {

			}

		}
		return null;
	}

	public AttachmentService getAttachmentService() {
		return attachmentService;
	}

	public void setAttachmentService(AttachmentService attachmentService) {
		this.attachmentService = attachmentService;
	}

	public CommonsMultipartResolver getMultipartResolver() {
		return multipartResolver;
	}

	public void setMultipartResolver(CommonsMultipartResolver multipartResolver) {
		this.multipartResolver = multipartResolver;
	}

}
