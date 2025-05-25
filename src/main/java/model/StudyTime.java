package model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "study_time")
public class StudyTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "study_date")
    private LocalDate studyDate;
    private double hours;
    public StudyTime() {
    }
    public StudyTime(User user, LocalDate studyDate, double hours) {
        this.user = user;
        this.studyDate = studyDate;
        this.hours = hours;
    }
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public LocalDate getStudyDate() {
		return studyDate;
	}
	public void setStudyDate(LocalDate studyDate) {
		this.studyDate = studyDate;
	}
	public double getHours() {
		return hours;
	}
	public void setHours(double hours) {
		this.hours = hours;
	}
    

}

