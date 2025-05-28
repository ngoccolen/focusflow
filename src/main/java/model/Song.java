package model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany(mappedBy = "songs")
    private List<User> users = new ArrayList<>();

    public int getId() { 
    	return id; 
    }
    public void setId(int id) { 
    	this.id = id; 
    }
    public int getSong_number() { 
    	return song_number; 
    }
    public void setSong_number(int song_number) { 
    	this.song_number = song_number; 
    }
    public String getSong_name() { 
    	return song_name; 
    }
    public void setSong_name(String song_name) { 
    	this.song_name = song_name; 
    }
    public String getDuration() { 
    	return duration; 
    }
    public void setDuration(String duration) { 
    	this.duration = duration; 
    }
    public String getFile_path() { 
    	return file_path; 
    }
    public void setFile_path(String file_path) { 
    	this.file_path = file_path; 
    }
    public List<User> getUsers() { 
    	return users; 
    }
    public void setUsers(List<User> users) { 
    	this.users = users; 
    }
}
