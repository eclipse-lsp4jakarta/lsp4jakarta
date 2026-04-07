package io.openliberty.sample.jakarta.annotations;

import java.io.IOException;

import jakarta.fake.PostConstruct;
import jakarta.fake.Resource;

@Resource(type = Object.class, name = "aa")
public class PostConstructAnnotation {

	private Integer studentId;

	private boolean isHappy;

	private boolean isSad;

	private String emailAddress;

	@PostConstruct()
	public Integer getStudentId() {
		return this.studentId;
	}

	@PostConstruct
	public void getHappiness(String type) {

	}

	@PostConstruct
	public void throwTantrum() throws Exception {
		System.out.println("I'm sad");
	}

	@PostConstruct
	public void throwRuntimeException() throws RuntimeException {
		System.out.println("RuntimeException");
	}

	@PostConstruct
	public void throwNullPointerException() throws NullPointerException {
		System.out.println("NullPointerException");
	}

	@PostConstruct
	public void throwIOException() throws IOException {
		System.out.println("IOException");
	}

	@PostConstruct
	public void throwExceptions() throws CustomCheckedException, CustomUncheckedException, IOException {
		System.out.println("throwExceptions");
	}

	@PostConstruct
	public void throwCustomUnCheckedException() throws CustomUncheckedException {
		System.out.println("CustomUncheckedException");
	}

	@PostConstruct
	public void throwError() throws Error {
		System.out.println("throwError");
	}
}
