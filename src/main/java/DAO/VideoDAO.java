package DAO;

import Util.HibernateUtil;
import model.User;
import model.Video;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class VideoDAO {

	public void saveIfNotExists(String fileName, String filePath, User user, String category) {
	    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
	        Query<Video> query = session.createQuery(
	            "FROM Video WHERE filePath = :filePath AND user = :user", Video.class);
	        query.setParameter("filePath", filePath);
	        query.setParameter("user", user);
	        Video existing = query.uniqueResult();

	        if (existing == null) {
	            Transaction tx = session.beginTransaction();
	            
	            Video video = new Video();
	            video.setFileName(fileName);
	            video.setFilePath(filePath);
	            video.setCategory(category);
	            video.setUser(user);
	            
	            session.save(video);
	            tx.commit();
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
    public List<Video> getByCategory(String category) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Video WHERE category = :categoryType", Video.class)
                          .setParameter("categoryType", category)
                          .list();
        }
    }
    public List<Video> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Video", Video.class).list();
        }
    }

}
