package io.openliberty.sample.jakarta.annotations;

import io.openliberty.sample.jakarta.di.Greeting;
import jakarta.annotation.Resource;

@Resource(type = Object.class, name = "")
class ResourceAnnotationTypeMismatch {

	@Resource(type = Object.class, name = "studentId")
    private Integer studentId;
    
	@Resource(type = Integer.class)
    private Integer mathsStudentId;
	
	@Resource(type = Integer.class)
    private Object itStudentId;
	
	@Resource(type = Greeting.class)
    private Integer bioStudentId;
	
	@Resource(name = "studentId")
    private Integer mechStudentId;
	
	@Resource(type = Greeting.class)
    private int englishStudentId;
	
	@Resource(type = Boolean.class)
    private boolean frenchhStudentId;
	
    @Resource
	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}

    @Resource(type = Integer.class)
	public void setStudentId1(Integer studentId) {
		this.studentId1 = studentId;
	}
    
    @Resource(type = Integer.class)
	public void setItStudentId(Object itStudentId) {
		this.itStudentId = itStudentId;
	}
    
    @Resource(type = Object.class)
	public void setMechStudentId(Integer mechStudentId) {
		this.mechStudentId = mechStudentId;
	}
    
    @Resource(type = Boolean.class)
	public void setFrenchhStudentId(boolean frenchhStudentId) {
		this.frenchhStudentId = frenchhStudentId;
	}
    
    private Integer studentId1;
} 
