package com.kien.qlsinhvien.model;


public class Student {
    private int id;
    private String studentId, fullName, address;
    private int age;
    private double averageScore;

    public Student(int id, String studentId, String fullName, int age, String address, double averageScore) {
        this.id = id;
        this.studentId = studentId;
        this.fullName = fullName;
        this.age = age;
        this.address = address;
        this.averageScore = averageScore;
    }
    
    public Student() {}

    //Ghi dữ liệu vào file
    public String toFileString() {
        return id + "," + studentId + "," + fullName + "," + age + "," + address + "," + averageScore;
    }

    //Chuyển sự liệu từ File thành đối tượng Student
    public static Student fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length != 6) throw new IllegalArgumentException("Dòng không đủ 6 trường.");

        int id = Integer.parseInt(parts[0].trim());
        String studentId = parts[1].trim();
        String fullName = parts[2].trim();
        int age = Integer.parseInt(parts[3].trim());
        String address= parts[4].trim();
        double averageScore = Double.parseDouble(parts[5].trim());

        return new Student(id, studentId, fullName, age, address, averageScore);
    }

    //Định dạng dữ liệu in ra màn hình
    @Override
    public String toString() {
        return String.format("ID: %d, Mã SV: %s, Họ Tên: %s, Tuổi: %d, Địa chỉ: %s, Điểm TB: %.2f",
                id, studentId, fullName, age, address, averageScore);
    }
    
    //Getters and Setters
    public int getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getFullName() { return fullName; }
    public int getAge() { return age; }
    public String getAddress() { return address; }
    public double getAverageScore() { return averageScore; }
    
    
    public void setId(int id) {this.id = id;}
	public void setStudentId(String studentId) {this.studentId = studentId;}
	public void setFullName(String fullName) { this.fullName = fullName; }
    public void setAge(int age) { this.age = age; }
    public void setAddress(String address) { this.address = address; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
}