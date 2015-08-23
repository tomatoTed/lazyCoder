package com.superted.lazyCoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
/**
 * 
 * @author Administrator
 *
 */
public class lazyCoder {
	private static Logger log = Logger.getLogger(lazyCoder.class.getName());
	private static lazyCoder lc=new lazyCoder();
	private static final String blank_4="\n    ";
	private static final String blank_8="\n        ";
	//设置合法的配置选项
	private static final List pathTypeList=Arrays.asList(new String[]{"controller","service","service","service-interface","serviceImpl","Dao","Dao-interface","DaoImpl"});
	public static void main(String[] args) throws IOException, TransformerException, SAXException, ParserConfigurationException, ClassNotFoundException{
		
		//lc.initConfigFile();//初始化配置文件
		//lc.parseConfigFile();//读取配置
		lc.createProject();//创建工程文件
	}
	//把路径转为URL
	//比如将com/lazyCoder/entity/user转为com.lazyCoder.entity.user
	private String dest2URL(String dest){
		String[] sp=dest.split("/",0);
		String entityURL="";//com.lazyCoder.entity.user
		for(int j=0;j<sp.length;j++){
			entityURL=entityURL+sp[j]+".";
		}
		return entityURL.replace(".java.", "");
	}
	//创建指定文件及目录
	 private  File createFile(String destFileName) {  
	        File file = new File(destFileName);  
	        if(file.exists()) {  
	        	file.delete();
	        }  
	        //判断目标文件所在的目录是否存在  
	        if(!file.getParentFile().exists()) {  
	            //如果目标文件所在的目录不存在，则创建父目录  
	            if(!file.getParentFile().mkdirs()) {  
	                log.error("创建目标文件所在目录失败！");  
	                return null;  
	            }  
	        }  
	        //创建目标文件  
	        try {  
	            if (file.createNewFile()) {  
	                log.debug("创建文件:"+destFileName+"成功");
	                return file;  
	            } else {  
	                log.error("创建文件:" + destFileName + "失败！");  
	                return null;  
	            }  
	        } catch (IOException e) {  
	            log.error("创建文件:" + destFileName + "失败！" + e.getMessage());  
	            return null;  
	        }  
	    }
	 //根据要生成的类型决定文件内容
	 private StringBuffer getContentByType(String pathType){
		 StringBuffer packageBuffer=new StringBuffer("package ");
		 return new StringBuffer();
	 }
	//创建文件的内容
	private void createContent(String pathDest,String pathAlias,String entityAlias,String pathType){
		String fileName=entityAlias+pathAlias+".java";//UserDao.java
		String fileDest=pathDest+"/"+fileName;//com/lazyCoder/Dao/UserDao.java
		try {
			File f=lc.createFile(System.getProperty("user.dir")+"/src/"+fileDest);//文件绝对路径
			FileWriter fw = new FileWriter(f);
			//定义java文件头部部分，包括引入包，作者说明，类定义
			StringBuffer headBuffer=new StringBuffer();
			headBuffer.append("package "+dest2URL(pathDest).replaceAll("\\.$", "")+";\n\n");
			headBuffer.append("/**");
			headBuffer.append("\n * aa@Author lazyCoder");
			headBuffer.append("\n * @date ");
			headBuffer.append("**/");
			headBuffer.append("public class " + dest2URL(fileName) +"{\n\n");
			//定义java文件内容部分，包括变量和方法
			StringBuffer contentBuffer=new StringBuffer();
			contentBuffer.append("}");
			getContentByType(pathType);
			fw.write("package "+dest2URL(pathDest).replaceAll("\\.$", "")+";\n\n"+"public class " + dest2URL(fileName) +"{\n}");
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//获取一个类中所有属性
	private List getField(String entityDest) throws ClassNotFoundException{
		Class c=Class.forName(entityDest);
		Field[] fields = c.getDeclaredFields();
		List resultList=new LinkedList();
		for(int i=0;i<fields.length;i++){
			Map resultMap=new HashMap();
			// AccessibleTest类中的成员变量为private,故必须进行此操作
			fields[i].setAccessible(true);
			//获取属性名称
			String fieldName = fields[i].getName();
			// 获取属性类型
			String fieldType = fields[i].getType().getName();
			resultMap.put("fieldName", fieldName);
			resultMap.put("fieldType", fieldType);
			resultList.add(resultMap);
			log.debug(fieldName+"  "+fieldType);
		}
		return resultList;
	}
	//创建工程文件
	public void createProject() throws SAXException, IOException, ParserConfigurationException, ClassNotFoundException{
		Map configMap=lc.parseConfigFile();//读取配置
		List entityList=(List)configMap.get("entityList");
		List pathList=(List)configMap.get("pathList");
		//遍历每个实体类，为实体类生成其他层文件
		for(int i=0;i<entityList.size();i++){
			List propertyList=new LinkedList();
			Map entityMap=(Map)entityList.get(i);
			String entityName=(String) entityMap.get("entityName");
			String entityDest=(String) entityMap.get("entityDest");//com/lazyCoder/entity/user.java
			String entityAlias=(String) entityMap.get("entityAlias");
			String entityURL=dest2URL(entityDest);//com.lazyCoder.entity.user
			//log.debug("entityURL="+entityURL);
			log.debug("创建文件中...");
			for(int j=0;j<pathList.size();j++){
				Map pathMap=(Map)pathList.get(j);
				String pathAlias=(String) pathMap.get("pathAlias");
				String pathDest=(String) pathMap.get("pathDest");
				String pathType=(String) pathMap.get("pathType");
				lc.createContent(pathDest,pathAlias,entityAlias,pathType);

			}
			//lc.getField(entityDest);
		}
		
	}
	//生成配置文件，并且初始化
	public void initConfigFile() throws IOException, TransformerException{
		 try {  
			// 解析器工厂类
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// 解析器
			DocumentBuilder builder = factory.newDocumentBuilder();
			// 操作的Document对象
			Document document = builder.newDocument();
			// 设置XML的版本
			document.setXmlVersion("1.0");
			//创建节点
			Element root = document.createElement("lazyCoder-config");//根节点
			Element pathConfig=document.createElement("pathConfig");//路径配置
			Element path=document.createElement("path");//路径配置
			Element entityMap=document.createElement("entityMap");//路径配置
			Element entity=document.createElement("entity");//路径配置
			Element frameworkConfig=document.createElement("frameworkConfig");//框架配置
			Element framework=document.createElement("framework");//框架配置
			String com_path="\n            配置工程目录\n    type决定了生成文件内的属性和方法，选项为：controller,service,service-interface,serviceImpl,Dao,Dao-interface,DaoImpl"
					+"\n    alias为别名，即最终写入工程目录的名字比如:\n    <path type=\"controller\" alias=\"Controller\">com/lazyCoder/controller</path>"
					+"\n            为user实体类最终生成的文件为:com.lazyCoder.controller.userController.java\n    ";
			Comment comment_path=document.createComment(com_path);//注释
			Comment comment_entity=document.createComment("配置工程实体");
			Comment comment_framework=document.createComment("配置工程使用的框架\n    type类型：ibatis,spring-mvc");
			// 设置节点之间的依赖关系
			document.appendChild(root);
			root.appendChild(document.createTextNode(blank_4));
			root.appendChild(comment_path);
			root.appendChild(document.createTextNode(blank_4));
			root.appendChild(pathConfig);
			pathConfig.appendChild(document.createTextNode(blank_8));
			pathConfig.appendChild(path);
			pathConfig.appendChild(document.createTextNode(blank_4));
			root.appendChild(document.createTextNode(blank_4));
			root.appendChild(comment_entity);
			root.appendChild(document.createTextNode(blank_4));
			root.appendChild(entityMap);
			root.appendChild(document.createTextNode(blank_4));
			root.appendChild(comment_framework);
			root.appendChild(document.createTextNode(blank_4));
			root.appendChild(frameworkConfig);
			frameworkConfig.appendChild(document.createTextNode(blank_8));
			frameworkConfig.appendChild(framework);
			frameworkConfig.appendChild(document.createTextNode(blank_4));
			entityMap.appendChild(document.createTextNode(blank_8));
			entityMap.appendChild(entity);
			entityMap.appendChild(document.createTextNode(blank_4));
			
			
			//设置节点的属性和值
			path.setAttribute("alias", "Controller");
			path.setAttribute("type", "controller");
			path.setAttribute("dest","com/lazyCoder/controller");
			framework.setAttribute("version", "2.3.0.677");
			framework.setAttribute("name", "ibatis");
			entity.setAttribute("alias", "User");
			entity.setAttribute("dest","com/lazyCoder/entity/user.java");
			
			// 开始把Document映射到文件
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transFormer = transFactory.newTransformer();
			transFormer.setOutputProperty(OutputKeys.INDENT, "yes"); 
			// 设置输出结果
			DOMSource domSource = new DOMSource(document);
			// 生成xml文件
			File file = new File("src/lazyCoder-config.xml");
			// 文件输出流
			FileOutputStream out = new FileOutputStream(file);
			// 设置输入源
			StreamResult xmlResult = new StreamResult(out);
			// 输出xml文件
			transFormer.transform(domSource, xmlResult);
			log.debug("配置文件初始化成功");
         } catch (ParserConfigurationException e) {  
            e.printStackTrace();
         } 
	}
	//读取配置文件
	private Map parseConfigFile() throws SAXException, IOException, ParserConfigurationException{
		 //取得DocumentBuilderFactory类的对象
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//取得DocumentBuilder类的对象
		DocumentBuilder builder = factory.newDocumentBuilder();
		//建立Document
		Document document = builder.parse("src/lazyCoder-config.xml");
		
		List entityList=new LinkedList();
		List pathList=new LinkedList();
		//建立entityNodeList
		NodeList entityNodeList = document.getElementsByTagName("entity");
		log.debug("配置的实体类为：");
		for(int i=0;i<entityNodeList.getLength();i++){
			String entityName="";
			String entityAlias="";
			String entityDest="";
			try{
				//读取实体类中的路径
				if(entityNodeList.item(i).getAttributes().getNamedItem("dest")!=null){
					entityDest=parseAttribute(entityNodeList.item(i).getAttributes().getNamedItem("dest").toString());
				}else{
					log.error("实体路径配置错误！");
					return null;
				}
				entityName=parseEntityName(entityDest);
				//读取实体类中的别名
				if(entityNodeList.item(i).getAttributes().getNamedItem("alias")!=null){
					entityAlias=parseAttribute(entityNodeList.item(i).getAttributes().getNamedItem("alias").toString());
				}else{
					entityAlias=entityName;
				}
				
				Map entityMap=new HashMap();
				entityMap.put("entityName", entityName);
				entityMap.put("entityDest", entityDest);
				entityMap.put("entityAlias", entityAlias);
				entityList.add(entityMap);
				
				log.debug("实体名称："+entityName+" 实体路径："+entityDest+" 实体别名："+entityAlias);
			}catch(Exception e){
				log.error("实体配置错误！");
				e.printStackTrace();
				return null;
			}
		}
		//建立pathNodeList
		NodeList pathNodeList = document.getElementsByTagName("path");
	
		log.debug("配置的目录为：");
		for(int i=0;i<pathNodeList.getLength();i++){
			String pathType="";
			String pathAlias="";
			String pathDest="";
			try {
			
				//读取目录中配置的路径
				if(pathNodeList.item(i).getAttributes().getNamedItem("dest")!=null){
					pathDest=parseAttribute(pathNodeList.item(i).getAttributes().getNamedItem("dest").toString());
				}else{
					log.error("错误，目录路径未配置！");
					return null;
				}
				//读取目录中配置的类型
				if(pathNodeList.item(i).getAttributes().getNamedItem("type")!=null){
					pathType=parseAttribute(pathNodeList.item(i).getAttributes().getNamedItem("type").toString());
					if(!pathTypeList.contains(pathType)){
						log.error("错误，不能识别"+pathType);
						return null;
					}
				}else{
					log.error("错误，目录类型未配置！");
					return null;
				}
				//读取目录中配置的别名
				if(pathNodeList.item(i).getAttributes().getNamedItem("alias")!=null){
					pathAlias=parseAttribute(pathNodeList.item(i).getAttributes().getNamedItem("alias").toString());
				}else{
					pathAlias=pathType;
				}
				Map pathMap=new HashMap();
				pathMap.put("pathDest", pathDest);
				pathMap.put("pathAlias", pathAlias);
				pathMap.put("pathType", pathType);
				pathList.add(pathMap);
				log.debug("目录类型："+pathType+" 目录路径："+pathDest+" 目录别名："+pathAlias);
			} catch (Exception e) {
				log.error("目录配置错误！");
				e.printStackTrace();
				return null;
			}
			
		}
		Map result=new HashMap();
		result.put("entityList", entityList);
		result.put("pathList", pathList);
		return result;
	}
	
	//解析attribute属性
	private static String parseAttribute(String str){
		return str.substring(str.indexOf("=")+2, str.length()-1);
	}
	//解析出entity的名称
	private static String parseEntityName(String str){
		return str.substring(str.lastIndexOf("/")+1,str.indexOf("."));
	}
	//返回当前时间或者日期
	private String date2String(String type){
		Date now=new Date();
		SimpleDateFormat dateFormat=new SimpleDateFormat("YYYY-MM-dd");
		SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm:ss");
		if("date".equals(type)){
			return dateFormat.format(now);
		}else if("time".equals(type)){
			return timeFormat.format(now);
		}
		return null;
	}
}
