package io.openliberty.sample.jakarta.annotations;

import jakarta.annotation.Priority;
import jakarta.annotation.Resource;

@Resource(type = Object.class, name = "aa")
@Priority(0)
public class ResourceAnnotation {

    private Integer studentId;


	@Resource(shareable = true)
    private boolean isHappy;

	@Resource(name = "test")
    private boolean isSad;


    private String emailAddress;


}

@Resource(name = "aa")
@Priority(-1)
class PostDoctoralStudent {

    private Integer studentId;


	@Resource(shareable = true)
    private boolean isHappy;

	@Resource
    private boolean isSad;


    private String emailAddress;

}

@Resource(type = Object.class)
@Priority(1)
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

    public Integer setStudentId1(@Priority(20) Integer studentId) {
        return studentId;
    }

    public void setStudentId3(@Priority(-20) Integer studentId) {
        this.studentId = studentId;
    }

    @Resource
    public void setIsHappy1(boolean isHappy) {
        this.isHappy = isHappy;
    }

    @Resource
    public void setIsSad(boolean isSad) {
        this.isSad = isSad;
    }

    private boolean isSad;

    private boolean isHappy;
} 
