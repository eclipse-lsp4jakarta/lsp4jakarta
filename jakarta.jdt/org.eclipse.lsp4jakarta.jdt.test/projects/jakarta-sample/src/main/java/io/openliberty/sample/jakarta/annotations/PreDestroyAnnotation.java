package io.openliberty.sample.jakarta.annotations;

import java.io.IOException;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

@Resource(type = Object.class, name = "aa")
public class PreDestroyAnnotation {

	private Integer studentId;

	private boolean isHappy;

	private boolean isSad;

	private String emailAddress;

	@PreDestroy()
	public Integer getStudentId() {
		return this.studentId;
	}

	@PreDestroy()
	public boolean getHappiness(String type) {
		if (type.equals("happy"))
			return this.isHappy;
		return this.isSad;
	}

	@PreDestroy()
	public static void makeUnhappy() {
		System.out.println("I'm sad");
	}

	@PreDestroy()
	public void throwTantrum() throws Exception {
		System.out.println("I'm sad");
	}

	@PreDestroy()
	public void throwRuntimeException() throws RuntimeException {
		System.out.println("RuntimeException");
	}

	@PreDestroy()
	public void throwNullPointerException() throws NullPointerException {
		System.out.println("NullPointerException");
	}

	@PreDestroy()
	public void throwIOException() throws IOException {
		System.out.println("IOException");
	}

	@PreDestroy()
	public void throwCustomExceptions() throws CustomCheckedException, CustomUncheckedException {
		System.out.println("throwCustomExceptions");
	}

	@PreDestroy()
	public void throwCustomUnCheckedException() throws CustomUncheckedException {
		System.out.println("CustomUncheckedException");
	}

	@PreDestroy()
	public void throwError() throws Error {
		System.out.println("throwError");
	}

}
