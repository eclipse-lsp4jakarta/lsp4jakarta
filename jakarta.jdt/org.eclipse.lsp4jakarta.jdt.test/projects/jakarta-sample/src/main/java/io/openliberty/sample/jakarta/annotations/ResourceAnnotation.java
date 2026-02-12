package io.openliberty.sample.jakarta.annotations;

import jakarta.annotation.Resource;
import jakarta.annotation.Resources;

@Resource(type = Object.class, name = "aa")
public class ResourceAnnotation {

    private Integer studentId;

	@Resource(shareable = true)
    private boolean isHappy;

	@Resource(name = "test")
    private boolean isSad;

    private String emailAddress;
    
    
    
}

@Resource(name = "aa")
class PostDoctoralStudent {

    private Integer studentId;


	@Resource(shareable = true)
    private boolean isHappy;

	@Resource
    private boolean isSad;


    private String emailAddress;

}

@Resource(type = Object.class)
class MasterStudent {

    private Integer studentId;
    
    @Resource
	public void setStudentId() {
		this.studentId = studentId;
	}
    
    @Resource
	public void getStudentId(Integer studentId) {
    	this.studentId = studentId;
	}

    @Resource
	public boolean setIsHappy(boolean isHappy) {
		return isHappy;
	}
   
    @Resource
	private void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}
    
    @Resource
 	public void setIsHappy1(boolean isHappy) {

 	}
    
    @Resource
    public void setIsSad(boolean isSad) {
		this.isSad = isSad;
	}
    
    private boolean isSad;
    
    private boolean isHappy;

} 
