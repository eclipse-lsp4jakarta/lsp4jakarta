package io.openliberty.sample.jakarta.annotations;

import jakarta.annotation.Priority;
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
@Priority(1)
class PostDoctoralStudent {

    private Integer studentId;


	@Resource(shareable = true)
    private boolean isHappy;

	@Resource
    private boolean isSad;


    private String emailAddress;

}

@Resource(type = Object.class)
@Priority(-1)
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
	public Integer setStudentId1(@Priority(1) Integer studentId) {
		return studentId;
	}
   
    @Resource
	public void setStudentId(@Priority(-1) Integer studentId) {
		this.studentId = studentId;
	}

} 
