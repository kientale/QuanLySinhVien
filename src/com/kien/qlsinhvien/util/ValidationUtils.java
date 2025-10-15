package com.kien.qlsinhvien.util;

public class ValidationUtils {

	// Kiểm tra mã sinh viên hợp lệ
	public static boolean isValidStudentId(String studentId) {
		if (studentId.trim().isEmpty()) {
			return false;
		} else {
			return studentId != null && studentId.matches("^SV\\d{3}$");
		}
	}

	// Tên chỉ gồm chữ cái và khoảng trắng, không chứa số hay ký tự đặc biệt
	public static boolean isValidName(String fullName) {
		if (fullName.trim().isEmpty()) {
			return false;
		} else {
			return fullName != null && fullName.matches("[a-zA-ZÀ-Ỹà-ỹ\\s]+");
		}
	}

	// Tuổi không được rỗng, phải là số, không chứa chữ/ký tự đặc biệt, và trong
	// khoảng 0–150
	public static boolean isValidAge(int age) {
	    return age >= 0 && age <= 150;
	}
	
	// Kiểm tra địa chỉ
	public static boolean isValidAddress(String address) {
		return !(address.trim().isEmpty());
	}

	public static boolean isValidAverageScore(double averageScore) {
	    return averageScore >= 0.0 && averageScore <= 10.0;
	}
}
