package com.springtemplate.attachment.dao;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.springtemplate.attachment.dto.Attachment;

@Repository("attachmentDao")
public class AttachmentDaoImpl implements AttachmentDao {

	private SimpleJdbcTemplate simpleJdbcTemplate;
	private JdbcTemplate jdbcTemplate;
	private LobHandler lobHandler = new DefaultLobHandler();
	private Logger logger = org.slf4j.LoggerFactory
			.getLogger(AttachmentDaoImpl.class);

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public boolean insertAttachment(Attachment attachment) {
		boolean insertStatus = false;
		InputStream is = null;
		BufferedInputStream bis = null;

		StringBuffer sqlSB = new StringBuffer();
		sqlSB.append("insert into attachment_data (filename, ");
		sqlSB.append("file_data, mime_type, description ");
		sqlSB.append(") VALUES (?, ?, ?, ?)");

		try {
			MultipartFile file = null;
			int[] paramTypes = new int[] { Types.VARCHAR, Types.BLOB,
					Types.VARCHAR, Types.VARCHAR };

			file = attachment.getUploadedFile();
			is = file.getInputStream();
			bis = new BufferedInputStream(is);
			Object[] params = { file.getOriginalFilename(),
					new SqlLobValue(bis, (int) file.getSize(), lobHandler),
					attachment.getMimeType(), attachment.getDescription()

			};
			jdbcTemplate.update(sqlSB.toString(), params, paramTypes);

			insertStatus = true;
		} catch (Exception e) {
			logger.debug(e.toString());
		} finally {
			try {
				if (bis != null) {
					bis.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException ex) {
				logger.debug(ex.toString());
			}
		}

		return insertStatus;
	}

	public boolean updateAttachments(List<String> updatedIdList,
			List<String> deletedIdList, Long requestId) {
		boolean insertStatus = false;

		StringBuffer sqlSB = new StringBuffer();
		try {
			if (updatedIdList != null && updatedIdList.size() > 0) {
				sqlSB.append("update attachment_data" +
						" set request_id = :requestId ")
						.append("where id in (:updatedIdList)");

				Map<String, Object> updateParams = new HashMap<String, Object>(
						2);
				updateParams.put("requestId", requestId);
				updateParams.put("updatedIdList", updatedIdList);

				simpleJdbcTemplate.update(sqlSB.toString(), updateParams);
			}

			if (deletedIdList != null && deletedIdList.size() > 0) {
				sqlSB.setLength(0);
				sqlSB.append("delete attachment where id in (:deletedIdList)");

				Map<String, Object> deleteParams = new HashMap<String, Object>(
						1);
				deleteParams.put("deletedIdList", deletedIdList);

				simpleJdbcTemplate.update(sqlSB.toString(), deleteParams);
			}

			insertStatus = true;

		} catch (Exception e) {
			logger.debug(e.toString());
		}

		return insertStatus;
	}

	public List<Attachment> getAttachmentList(Long requestId) {
		StringBuffer sqlSB = new StringBuffer();
		sqlSB.append("select a.id, a.filename, ");
		sqlSB.append("a.description ");
		sqlSB.append("from attachment_data a ");

		List<Attachment> attachmentList = simpleJdbcTemplate.query(sqlSB
				.toString(), new AttachmentRowMapper());

		logger.info("sqlSB.toString() " + sqlSB.toString());
		return attachmentList;
	}

	public Attachment getDispAttachment(Long requestId) {
		StringBuffer sqlSB = new StringBuffer();
		sqlSB.append("select a.id,  a.filename, ");
		sqlSB.append("a.description  from attachement_data ");

		Attachment attachment = null;
		try {
			attachment = simpleJdbcTemplate.queryForObject(sqlSB.toString(),
					new AttachmentRowMapper());
		} catch (EmptyResultDataAccessException e) {
			logger.debug("getDispAttachment return null");
		}
		return attachment;
	}

	public List<Attachment> getAttachmentList(List<String> attachmentIdList,
			List<String> deletedAttachmentIdList, Long requestId) {
		StringBuffer sqlSB = new StringBuffer();

		if (deletedAttachmentIdList != null) {
			sqlSB.append("delete attachment where id in (:deletedIdList)");
			Map<String, Object> deleteParams = new HashMap<String, Object>(1);
			deleteParams.put("deletedIdList", deletedAttachmentIdList);
			simpleJdbcTemplate.update(sqlSB.toString(), deleteParams);
		}

		if (attachmentIdList == null || attachmentIdList.size() == 0) {
			attachmentIdList = new ArrayList<String>();
			attachmentIdList.add("0");
		}

		Map<String, Object> searchParams = new HashMap<String, Object>(2);
		searchParams.put("attachmentIdList", attachmentIdList);
		searchParams.put("requestId", requestId);
		sqlSB.setLength(0);
		sqlSB.append("select a.id,  a.filename, ");
		sqlSB.append("a.description, from attachment_data ");

		List<Attachment> attachmentList = simpleJdbcTemplate.query(sqlSB
				.toString(), new AttachmentRowMapper(), searchParams);

		return attachmentList;
	}

	public Attachment downloadAttachment(String attachmentId) {
		StringBuffer sqlSB = new StringBuffer();
		sqlSB.append("select id,filename, file_data, mime_type from attachment_data ")
				.append("where id = ?");

		Attachment downloadObj = null;

		try {
			downloadObj = jdbcTemplate.queryForObject(sqlSB.toString(),
					new DownloadRowMapper(), attachmentId);
		} catch (EmptyResultDataAccessException e) {
			logger.debug("AttachmentDaoImpl.downloadAttachment return null");
		}

		return downloadObj;
	}

	

	public SimpleJdbcTemplate getSimpleJdbcTemplate() {
		return simpleJdbcTemplate;
	}

	public void setSimpleJdbcTemplate(SimpleJdbcTemplate simpleJdbcTemplate) {
		this.simpleJdbcTemplate = simpleJdbcTemplate;
	}

	private class AttachmentRowMapper implements RowMapper<Attachment> {
		public Attachment mapRow(ResultSet rs, int rowNum) throws SQLException {
			Attachment attachment = new Attachment();

			attachment.setAttachmentId(rs.getInt("id"));
			attachment.setFilename(rs.getString("filename"));
			attachment.setDescription(rs.getString("description"));

			return attachment;
		}
	};

	private class DownloadRowMapper implements RowMapper<Attachment> {
		public Attachment mapRow(ResultSet rs, int rowNum) throws SQLException {
			Attachment attachment = new Attachment();
			attachment.setAttachmentId(rs.getInt("id"));
			attachment.setFilename(rs.getString("filename"));
			attachment.setFileData(rs.getBlob("file_data"));
			attachment.setMimeType(rs.getString("mime_type"));
			return attachment;
		}
	};
}
