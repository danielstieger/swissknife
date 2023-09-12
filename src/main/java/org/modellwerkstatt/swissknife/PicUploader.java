package org.modellwerkstatt.swissknife;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


// TODO: Exception handling, log via servlet logger on problems.

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 5 * 5)
public class PicUploader extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private boolean serveLocalViaStatic = false;

    private String uploadPath;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String filesystemFileDestination = getInitParameter("filesystemFileDestination");
        if (getInitParameter("serveLocalViaStatic") != null) {
            serveLocalViaStatic = "true".equals(getInitParameter("serveLocalViaStatic").toLowerCase().trim());
        }

        if (! serveLocalViaStatic && filesystemFileDestination == null) {
            throw new RuntimeException("serveLocalViaStatic is false and filesystemFileDestination init-param is not given.");
        }

        if (serveLocalViaStatic) {
            uploadPath =  getServletContext().getRealPath("") + File.separator + "/static/upload";
        } else {
            uploadPath = filesystemFileDestination;
        }

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            if (! uploadDir.mkdir()) {
                // TODO - what should we log here?
                throw new RuntimeException("Can not create directory " + uploadDir.getAbsolutePath());
            }
        }

    }

    private String getFileName(Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename"))
                return content.substring(content.indexOf("=") + 2, content.length() - 1);
        }
        return "DEFAULT_FILENAME";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().println("?");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String returnMsg = "";
        try {
            for (Part part : request.getParts()) {
                String fileName = getFileName(part);

                if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                    part.write(uploadPath + File.separator + fileName);
                    returnMsg += fileName + " ";
                }
            }

        } catch (FileNotFoundException fne) {
            returnMsg += "ERROR " + fne.getMessage();
            // TODO? What should we log here.

        }

        response.getWriter().println(returnMsg);
    }
}