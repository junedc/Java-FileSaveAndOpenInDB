package com.springtemplate.attachment.service;

import java.util.List;

import com.springtemplate.attachment.dto.Attachment;

/**
 * @author Wawan Kartawan
 * 
 */
public interface AttachmentService {

    public boolean insertAttachment(Attachment attachment);

    public boolean updateAttachments(List<String> updatedIdList,
            List<String> deletedIdList, Long requestId);

    public List<Attachment> getAttachmentList(Long requestId);

    public Attachment getDispAttachment(Long requestId);

    public List<Attachment> getAttachmentList(List<String> attachmentIdList,
            List<String> deletedAttachmentIdList, Long requestId);

    public Attachment downloadAttachment(String attachmentId);
	
	
}
