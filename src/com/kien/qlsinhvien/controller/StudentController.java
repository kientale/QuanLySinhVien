package com.kien.qlsinhvien.controller;

import com.kien.qlsinhvien.service.StudentService;
import java.util.Scanner;

public class StudentController {
    public static void main(String[] args) {
        StudentService manager = new StudentService("file/student_file.txt");
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n----- MENU -----");
            System.out.println("1. Hiển thị danh sách sinh viên");
            System.out.println("2. Thêm sinh viên");
            System.out.println("3. Tìm kiếm sinh viên theo mã sinh viên");
            System.out.println("4. Cập nhật thông tin sinh viên");
            System.out.println("5. Xóa sinh viên");
            System.out.println("6. Kiểm tra dữ liệu");
            System.out.println("0. Thoát");
            System.out.print("Chọn: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> manager.displayStudents();
                case 2 -> manager.addStudent(scanner);
                case 3 -> manager.findStudent(scanner);
                case 4 -> manager.updateStudent(scanner);
                case 5 -> manager.deleteStudent(scanner);
                case 6 -> manager.validateStudentsFromFile();
                case 0 -> System.out.println("Thoát chương trình.");
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        } while (choice != 0);
    }
}