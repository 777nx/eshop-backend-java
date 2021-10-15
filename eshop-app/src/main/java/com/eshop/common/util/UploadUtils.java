package com.eshop.common.util;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.UUID;


@Component
public class UploadUtils {

    @Autowired
    private FileProperties fileProperties;
    private static FileProperties configFileProperties;

    @PostConstruct
    public void init(){
        configFileProperties = this.fileProperties;
    }


    public static String getImgUrl(MultipartFile file){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String url ="";
        try {
            //读取application.properties文件里面的数据
//            Properties prop=new Properties();
//            System.out.println(prop);
//            prop.load(new InputStreamReader(Client.class.getClassLoader().getResourceAsStream("application-dev.yml"),"UTF-8"));
//            url = (String)prop.get("avatar");
            url = configFileProperties.getPath();
        }catch (Exception e){
            throw new RuntimeException("字节流转化字符流异常，请联系管理员！");
        }
        //获取文件名称
        String fileName = file.getOriginalFilename();
        String extendedName = FilenameUtils.getExtension(file.getOriginalFilename());
        String newFileName = UUID.randomUUID().toString().replaceAll("-", "") + "."+extendedName;

        //文件上传
        File dir = new File(url);
        if(!dir.exists()){
            dir.mkdirs();
        }
        if(!extendedName.equals("jpg")&&!extendedName.equals("png")){
            throw new RuntimeException("上传文件只能是jpg或png格式！");
        }
        //判断上传文件是否为空
        if(!file.isEmpty()){
            try {
                File f = new File(url,newFileName);
                file.transferTo(f);
                System.out.println(f.getPath());
            }catch (Exception e){
                e.printStackTrace();
                throw new RuntimeException("文件上传失败，请联系管理管理员！");
            }
        }else{
            throw new RuntimeException("未接收到文件，请重新尝试！");
        }
        return "/file/"+newFileName;
    }
}
