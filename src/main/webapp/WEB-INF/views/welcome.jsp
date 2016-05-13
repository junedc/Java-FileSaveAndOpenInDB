<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
<head>
<title>Title</title>


<script src="scripts/jquery.min.js"></script>
<script type="text/javascript" src="scripts/jquery.iframe-post-form.js"></script>
<script type="text/javascript">
            $(document).ready(function() {
                //alert("ready");
            });


            $(function (){
            	$("#submitUploadButton").click(function(){
                	
            		var completeCount = 0;
            	
            		var filename = "";
            	    $('form[id=fileUploadForm]').iframePostForm ({
            	        post : function (){
            	        
            	    		var fullPathFile = document.getElementById("fileId").value;
            	    		
            	    		filename = fullPathFile.substring(fullPathFile.lastIndexOf("\\")+1);
            				var form = document.getElementById("fileUploadForm");
            				form.enctype="multipart/form-data" ;
            				form.encoding="multipart/form-data" ;
            				form.action="insertattachment";
            	           
            	        },
            	        complete : function (data){
            		      
            	            var idx = data.indexOf("error");
            	              if(completeCount==0){ 
            		            if(idx!=-1){
            		            	var response2 = data.substring(0,idx);
            		            	alert(response2);
            		            }else{
            						//alert("Please select the correct file path.");
            		            }           
            	            	completeCount++;
            	            }
            	           
            	        }
            	    });
            	});
            });

            
        </script>
</head>
<body>
<form id="fileUploadForm" method="post" >
<p><strong>Tip:</strong> Use the Control or the Shift key to select multiple files.</p>

<input type="file" id="fileId" name="uploadedFile" > 
<input type="text" id="description" name="description"> 
<input id="submitUploadButton" type="submit" value="Upload"  />
<input type="hidden" id="selectedFileId" name="selectedFileId" />



<table>
	<c:forEach var="attachment" items="${attachments}">
		<tr>
			<td><c:out value="${attachment.attachmentId}" /></td>
			<td><c:out value="${attachment.filename}" /></td>
			<td><c:out value="${attachment.description}" /></td>
			<td><img src='images/download.gif'
				onclick='downloadFile(${attachment.attachmentId})'
				style='cursor: pointer;' /></td>
		</tr>
	</c:forEach>
</table>

<br>
<br>
<br>
Lesson learn. Submit button must be inside the forms forms enclosure.<br>
<br>
1</form>




</body>
</html>