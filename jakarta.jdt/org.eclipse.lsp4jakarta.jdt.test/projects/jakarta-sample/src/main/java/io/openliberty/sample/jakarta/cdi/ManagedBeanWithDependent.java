package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.*;

@Dependent
@RequestScoped
public class ManagedBeanWithDependent<T> {
	public int a;

	public ManagedBeanWithDependent() {
		this.a = 10;
	}
}

@Dependent
@RequestScoped
@SessionScoped
class NonGenericManagedBean {
	public int a;

	public NonGenericManagedBean() {
		this.a = 10;
	}
}

@RequestScoped
@SessionScoped
class ManagedBeanWithoutDependent<T>  {
	public static int a;

	public ManagedBeanWithoutDependent() {
		this.a = 10;
	}
}

@RequestScoped
@SessionScoped
class ManagedBeanWithMultipleScopes2 {
	public static int a;

	public ManagedBeanWithMultipleScopes2() {
		this.a = 10;
	}
}