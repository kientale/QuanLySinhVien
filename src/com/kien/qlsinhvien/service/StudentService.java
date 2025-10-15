package com.kien.qlsinhvien.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.kien.qlsinhvien.model.Student;
import com.kien.qlsinhvien.util.ValidationUtils;

public class StudentService {
	private final String filePath;

	// Kiểm tra trùng lặp id và mã sinh viên
	public boolean isDuplicateStudent(int id, String studentId) {
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.isBlank())
					continue;

				Student student = Student.fromFileString(line);
				if (student == null)
					continue;

				if (student.getId() == id || student.getStudentId().equalsIgnoreCase(studentId)) {
					return true;
				}
			}
		} catch (IOException e) {
			System.out.println("Lỗi khi kiểm tra trùng lặp: " + e.getMessage());
		}
		return false;
	}

	// Constructor tạo file nếu chưa tồn tại ngay khi ta chạy ứng dụng
	public StudentService(String filePath) {
		this.filePath = filePath;
		File file = new File(filePath);
		if (!file.exists()) { // Nếu chưa tồn tại thì tạo file mới
			try {
				file.getParentFile().mkdirs();// mkdirs() dùng khi tạo file có thư mục cha
				file.createNewFile();
			} catch (IOException e) {
				System.out.println("Không thể tạo file: " + e.getMessage());
			}
		}
	}

	public void validateStudentsFromFile() {
		Set<Integer> seenIds = new HashSet<>();
		Set<Integer> duplicateIds = new HashSet<>();

		Set<String> seenStudentIds = new HashSet<>();
		Set<String> duplicateStudentIds = new HashSet<>();

		AtomicInteger lineNumber = new AtomicInteger(1);// Là 1 lớp Wrapper cho kiểu int để tự tăng biến lineNumber khi
														// duyệt dòng
		boolean[] validStudent = { true };

		System.out.println("=== Kiểm tra dữ liệu sinh viên từ file ===");

		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			br.lines().forEach(line -> {
				int currentLine = lineNumber.getAndIncrement();// dùng currentLine để hiển thị lỗi hoặc cảnh báo liên
																// quan đến dòng đang xử lý
				if (line.isBlank())
					return;

				Student student = parseStudent(line, currentLine);
				if (student == null) {
					validStudent[0] = false;
					return;
				}

				if (!validateStudent(student, currentLine)) {
					validStudent[0] = false;
				}

				// Kiểm tra trùng ID
				if (!seenIds.add(student.getId())) {
					duplicateIds.add(student.getId());
				}

				// Kiểm tra trùng mã sinh viên
				if (!seenStudentIds.add(student.getStudentId())) {
					duplicateStudentIds.add(student.getStudentId());
				}
			});
		} catch (IOException e) {
			System.out.println("Lỗi khi đọc file: " + e.getMessage());
			return;
		}

		if (!duplicateIds.isEmpty()) {
			validStudent[0] = false;
			System.out.println("\n=== Cảnh báo: Có id bị trùng ===");
			duplicateIds.forEach(id -> System.out.println("  - id trùng: " + id));
		}

		if (!duplicateStudentIds.isEmpty()) {
			validStudent[0] = false;
			System.out.println("\n=== Cảnh báo: Có mã sinh viên bị trùng ===");
			duplicateStudentIds.forEach(msv -> System.out.println("  - Mã sinh viên trùng: " + msv));
		}

		if (validStudent[0]) {
			System.out.println("Dữ liệu hợp lệ");
		}
	}

	// Tách và kiểm tra dữ liệu trên từng dòng từ file
	private Student parseStudent(String line, int lineNumber) {
		String[] parts = line.split(",");

		if (parts.length != 6) {
			System.out.printf(">> Dòng %d: Không đủ trường dữ liệu.\n  Nội dung: %s\n", lineNumber, line);
			return null;
		}

		// Dùng map để trim tất cả phần tử và gán tên
		String idStr = parts[0].trim();
		String studentId = parts[1].trim();
		String fullName = parts[2].trim();
		String ageStr = parts[3].trim();
		String address = parts[4].trim();
		String avgScoreStr = parts[5].trim();

		// Kiểm tra rỗng
		if (Stream.of(idStr, studentId, fullName, ageStr, address, avgScoreStr).anyMatch(String::isEmpty)) {
			System.out.printf(">> Dòng %d: Thiếu thông tin bắt buộc.\n  Nội dung: %s\n", lineNumber, line);
			return null;
		}

		try {
			int id = Integer.parseInt(idStr);
			int age = Integer.parseInt(ageStr);
			double avgScore = Double.parseDouble(avgScoreStr);
			return new Student(id, studentId, fullName, age, address, avgScore);
		} catch (NumberFormatException e) {
			System.out.printf(">> Dòng %d: Lỗi định dạng dữ liệu.\n  Nội dung: %s\n", lineNumber, line);
			return null;
		}
	}

	// Hàm kiểm tra thông tin của 1 student
	// Kiểm tra định dạng của 1 sinh viên
	private boolean validateStudent(Student student, int lineNumber) {
		boolean isValid = true;

		if (!ValidationUtils.isValidStudentId(student.getStudentId())) {
			System.out.printf(">> Dòng %d: %s\n  - Mã sinh viên không hợp lệ (%s)\n", lineNumber, student,
					student.getStudentId());
			isValid = false;
		}

		if (!ValidationUtils.isValidAge(student.getAge())) {
			System.out.printf(">> Dòng %d: %s\n  - Tuổi không hợp lệ (%d)\n", lineNumber, student, student.getAge());
			isValid = false;
		}

		if (!ValidationUtils.isValidAverageScore(student.getAverageScore())) {
			System.out.printf(">> Dòng %d: %s\n  - Điểm trung bình không hợp lệ (%.2f)\n", lineNumber, student,
					student.getAverageScore());
			isValid = false;
		}

		return isValid;
	}

	// Nhập mã sinh viên hợp lệ và không trùng
	public String inputStudentId(int newId, String studentId, Scanner scanner) {
		do {
			System.out.print("Nhập mã SV: ");
			studentId = scanner.nextLine().trim();

			if (!ValidationUtils.isValidStudentId(studentId)) {
				System.out.println("❌ Mã SV không hợp lệ (phải có dạng SVxxx).");
			} else if (isDuplicateStudent(newId, studentId)) {
				System.out.println("❌ Mã SV đã tồn tại, vui lòng nhập lại.");
			}
		} while (!ValidationUtils.isValidStudentId(studentId) || isDuplicateStudent(newId, studentId));

		return studentId;
	}

	// Nhập họ tên hợp lệ
	public String inputFullName(String fullName, Scanner scanner) {
		do {
			System.out.print("Nhập họ tên: ");
			fullName = scanner.nextLine().trim();

			if (!ValidationUtils.isValidName(fullName)) {
				System.out.println("❌ Họ tên không hợp lệ. Vui lòng nhập lại.");
			}
		} while (!ValidationUtils.isValidName(fullName));

		return fullName;
	}

	// Nhập tuổi hợp lệ (số nguyên từ 0-150)
	public int inputAge(int age, Scanner scanner) {
		while (true) {
			System.out.print("Nhập tuổi: ");
			String input = scanner.nextLine().trim();

			if (!input.matches("\\d+")) {
				System.out.println("❌ Tuổi không hợp lệ. Vui lòng nhập số từ 0 đến 150.");
				continue;
			}

			age = Integer.parseInt(input);
			if (ValidationUtils.isValidAge(age)) {
				return age;
			}
			System.out.println("❌ Tuổi phải từ 0 đến 150.");
		}
	}

	// Nhập địa chỉ hợp lệ (không rỗng)
	public String inputAddress(String address, Scanner scanner) {
		do {
			System.out.print("Nhập địa chỉ: ");
			address = scanner.nextLine().trim();

			if (!ValidationUtils.isValidAddress(address)) {
				System.out.println("❌ Địa chỉ không được để trống.");
			}
		} while (!ValidationUtils.isValidAddress(address));

		return address;
	}

	// Nhập điểm trung bình hợp lệ (từ 0.0 đến 10.0)
	public double inputAverageScore(double averageScore, Scanner scanner) {
		while (true) {
			System.out.print("Nhập điểm TB: ");
			String input = scanner.nextLine().trim();

			if (!input.matches("^\\d+(\\.\\d+)?$")) {
				System.out.println("❌ Điểm TB không hợp lệ. Nhập số từ 0.0 đến 10.0.");
				continue;
			}

			averageScore = Double.parseDouble(input);
			if (ValidationUtils.isValidAverageScore(averageScore)) {
				return averageScore;
			}
			System.out.println("❌ Điểm TB phải từ 0.0 đến 10.0.");
		}
	}

	// Hiển thị danh sách sinh viên từ file, kèm kiểm tra dữ liệu
	public void displayStudents() {
	    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
	        String line;
	        boolean isEmpty = true;
	        int lineNumber = 0;

	        while ((line = br.readLine()) != null) {
	            lineNumber++;

	            if (line.trim().isEmpty()) continue; // Bỏ qua dòng trắng
	            isEmpty = false;

	            // ✅ Gọi parseStudent để kiểm tra thiếu trường, rỗng, sai định dạng
	            Student student = parseStudent(line, lineNumber);
	            if (student == null) {
	                continue; // Đã in lỗi trong parseStudent
	            }

	            StringJoiner errorMessages = new StringJoiner("\n  ");

	            // Kiểm tra các điều kiện hợp lệ
	            if (!ValidationUtils.isValidStudentId(student.getStudentId())) {
	                errorMessages.add("Lỗi: Mã sinh viên sai định dạng (SVxxx): " + student.getStudentId());
	            }
	            if (!ValidationUtils.isValidAge(student.getAge())) {
	                errorMessages.add("Lỗi: Tuổi không hợp lệ: " + student.getAge());
	            }
	            if (!ValidationUtils.isValidAverageScore(student.getAverageScore())) {
	                errorMessages.add("Lỗi: Điểm trung bình không hợp lệ (0-10): " + student.getAverageScore());
	            }

	            // In thông tin sinh viên hoặc lỗi nếu có
	            if (errorMessages.length() == 0) {
	                System.out.println(student + "\n");
	            } else {
	                System.out.println(student);
	                System.out.println("⚠️ Thông tin không hợp lệ:\n  " + errorMessages + "\n");
	            }
	        }

	        if (isEmpty) {
	            System.out.println("📁 File không có dữ liệu.");
	        }

	    } catch (IOException e) {
	        System.out.println("❌ Lỗi đọc file: " + e.getMessage());
	    }
	}


	public void addStudent(Scanner scanner) {
		while (true) {
			try {
				int newId = getLastId() + 1;

				// Nhập mã sinh viên
				String studentId = null;
				studentId = inputStudentId(newId, studentId, scanner);

				String fullName = null;
				fullName = inputFullName(fullName, scanner);

				int age = 0;
				age = inputAge(age, scanner);

				String address = null;
				address = inputAddress(address, scanner);

				double averageScore = 0;
				averageScore = inputAverageScore(averageScore, scanner);

				Student sv = new Student(newId, studentId, fullName, age, address, averageScore);
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
					bw.write(sv.toFileString());
					bw.newLine();
					System.out.println("Thêm sinh viên thành công!");
				}
				break; // kết thúc vòng lặp nếu thành công
			} catch (Exception e) {
				System.out.println("Thêm thất bại: " + e.getMessage());
			}
		}
	}

	private int getLastId() {
		int lastId = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				Student sv = Student.fromFileString(line);
				if (sv.getId() > lastId)
					lastId = sv.getId();
			}
		} catch (IOException e) {
			System.out.println("Không thể đọc ID cuối: " + e.getMessage());
		}
		return lastId;
	}

	public void findStudent(Scanner scanner) {
	    String studentId;
	    do {
	        System.out.print("Nhập mã SV: ");
	        studentId = scanner.nextLine().trim();

	        if (!ValidationUtils.isValidStudentId(studentId)) {
	            System.out.println("Mã SV không hợp lệ (phải có dạng SVxxx).");
	        }
	    } while (!ValidationUtils.isValidStudentId(studentId));

	    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
	        String line;
	        int count = 0;
	        int lineNumber = 0;
	        List<Student> danhSachStudentTimKiem = new ArrayList<>();

	        while ((line = br.readLine()) != null) {
	            lineNumber++;

	            if (line.trim().isEmpty()) continue;

	            // ✅ Dùng parseStudent để kiểm tra dữ liệu trước
	            Student sv = parseStudent(line, lineNumber);
	            if (sv == null) {
	                continue; // Đã in lỗi trong parseStudent
	            }

	            if (sv.getStudentId().equalsIgnoreCase(studentId)) {
	                danhSachStudentTimKiem.add(sv);
	                count++;
	            }
	        }

	        for (Student student : danhSachStudentTimKiem) {
	            System.out.println(student);
	        }

	        if (count == 0) {
	            System.out.println("Không tìm thấy sinh viên.");
	        } else if (count > 1) {
	            System.out.println("⚠️ Lỗi! Có sinh viên bị trùng studentId.");
	        }

	    } catch (IOException e) {
	        System.out.println("❌ Lỗi tìm sinh viên: " + e.getMessage());
	    }
	}


	public void updateStudent(Scanner scanner) {
		String studentId;
		do {
			System.out.print("Nhập mã SV: ");
			studentId = scanner.nextLine().trim();
			if (!ValidationUtils.isValidStudentId(studentId)) {
				System.out.println("Mã SV không hợp lệ (phải có dạng SVxxx).");
			}
		} while (!ValidationUtils.isValidStudentId(studentId));

		File inputFile = new File(filePath);
		File tempFile = new File("file/temp.txt");

		List<String> lines = new ArrayList<>();
		List<Student> matchedStudents = new ArrayList<>();
        int lineNumber = 0;


		// Đọc toàn bộ file vào bộ nhớ
		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
				Student sv = parseStudent(line, lineNumber);
	            if (sv == null) {
	                continue; // Đã in lỗi trong parseStudent
	            }
				
				if (sv.getStudentId().trim().equalsIgnoreCase(studentId)) {
					matchedStudents.add(sv);
				}
			}
		} catch (IOException e) {
			System.out.println("Lỗi khi đọc file: " + e.getMessage());
			return;
		}

		if (matchedStudents.isEmpty()) {
			System.out.println("Không tìm thấy sinh viên để cập nhật.");
			return;
		}

		if (matchedStudents.size() > 1) {
			System.out.println("Lỗi: Phát hiện nhiều hơn 1 sinh viên có cùng mã: " + studentId);
			return;
		}

		// Tiến hành cập nhật
		Student updatedStudent = matchedStudents.get(0);
		System.out.println("Thông tin cũ: " + updatedStudent);
		updatedStudent.setFullName(inputFullName(updatedStudent.getFullName(), scanner));
		updatedStudent.setAge(inputAge(updatedStudent.getAge(), scanner));
		updatedStudent.setAddress(inputAddress(updatedStudent.getAddress(), scanner));
		updatedStudent.setAverageScore(inputAverageScore(updatedStudent.getAverageScore(), scanner));

		// Ghi lại file với thông tin đã cập nhật
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
			for (String line : lines) {
				Student sv = Student.fromFileString(line);
				if (sv.getStudentId().trim().equalsIgnoreCase(studentId)) {
					bw.write(updatedStudent.toFileString());
				} else {
					bw.write(line);
				}
				bw.newLine();
			}
		} catch (IOException e) {
			System.out.println("Lỗi khi ghi file tạm: " + e.getMessage());
			return;
		}

		// Ghi đè file cũ bằng file tạm
		try {
			Files.deleteIfExists(inputFile.toPath());
			Files.move(tempFile.toPath(), inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Cập nhật thành công.");
		} catch (IOException e) {
			System.out.println("Lỗi khi thay thế file cũ: " + e.getMessage());
		}
	}

	public void deleteStudent(Scanner scanner) {
		System.out.print("Nhập mã SV cần xóa: ");
		String maSV = scanner.nextLine().trim();

		File inputFile = new File(filePath);
		File tempFile = new File("file/temp.txt");
		boolean hasDeletedAny = false;
        int lineNumber = 0;


		try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
				BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

			String line;
			while ((line = br.readLine()) != null) {
				Student sv = parseStudent(line, lineNumber);
	            if (sv == null) {
	                continue; // Đã in lỗi trong parseStudent
	            }
				if (sv.getStudentId().equalsIgnoreCase(maSV)) {
					System.out.println("Tìm thấy: " + sv);
					System.out.print("Xác nhận xóa (y/n)? ");
					String confirm = scanner.nextLine().trim();
					if (confirm.equalsIgnoreCase("y")) {
						hasDeletedAny = true;
						continue; // Bỏ qua ghi dòng này (xóa)
					}
				}

				// Ghi dòng không bị xóa
				bw.write(sv.toFileString());
				bw.newLine();
			}

		} catch (IOException e) {
			System.out.println("Lỗi xóa: " + e.getMessage());
			return;
		}

		// Thao tác thay thế sau khi file reader/writer đã đóng
		if (hasDeletedAny) {
			try {
				Files.deleteIfExists(inputFile.toPath());
				Files.move(tempFile.toPath(), inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Xóa sinh viên thành công.");
			} catch (IOException e) {
				System.out.println("Lỗi khi thay thế file: " + e.getMessage());
			}
		} else {
			tempFile.delete();
			System.out.println("Không xóa sinh viên nào.");
		}
	}
}