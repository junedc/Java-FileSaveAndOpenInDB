package com.springtemplate.attachment.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springtemplate.attachment.dao.AttachmentDao;
import com.springtemplate.attachment.dto.Attachment;

/**
 * @author Wawan Kartawan
 * 
 */
@Service("attachmentService")
public class AttachmentServiceImpl implements AttachmentService {

    @Autowired
    private AttachmentDao attachmentDao;

    public boolean insertAttachment(Attachment attachment) {
        return attachmentDao.insertAttachment(attachment);
    }
	


    public boolean updateAttachments(List<String> updatedIdList,
            List<String> deletedIdList, Long requestId) {
        return attachmentDao.updateAttachments(updatedIdList, deletedIdList,
                requestId);
    }

    public List<Attachment> getAttachmentList(Long requestId) {
        return attachmentDao.getAttachmentList(requestId);
    }

    public Attachment getDispAttachment(Long requestId) {
        return attachmentDao.getDispAttachment(requestId);
    }

    public List<Attachment> getAttachmentList(List<String> attachmentIdList,
            List<String> deletedAttachmentIdList, Long requestId) {
        return attachmentDao.getAttachmentList(attachmentIdList,
                deletedAttachmentIdList, requestId);
    }

    public Attachment downloadAttachment(String attachmentId) {
        return attachmentDao.downloadAttachment(attachmentId);
    }

    public AttachmentDao getAttachmentDao() {
        return attachmentDao;
    }

    public void setAttachmentDao(AttachmentDao attachmentDao) {
        this.attachmentDao = attachmentDao;
    }
}
