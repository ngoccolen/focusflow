package model;

import jakarta.persistence.*;

@Entity
@Table(name = "songs")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int song_number;
    private String song_name;
    private String duration;
    private String file_path;

    @ManyToOne
    @JoinColumn(name = "user_id") // tên cột khóa ngoại trong bảng songs
    private User user;

    // Getters và setters
    public int getId() { 
    	return id; 
    }
    public void setId(int id) { this.id = id; }

    public int getSong_number() { return song_number; }
    public void setSong_number(int song_number) { this.song_number = song_number; }

    public String getSong_name() { return song_name; }
    public void setSong_name(String song_name) { this.song_name = song_name; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getFile_path(String file_path) {
    	return file_path;
    }
    public void setFile_path(String file_path) {
    	this.file_path = file_path;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
