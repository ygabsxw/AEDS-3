package model;

import java.util.*;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class Movie {
    private int show_id;
    private String type;
    private String title;
    private String director;
    private String[] cast;
    private String country;
    private String date_added;
    private int release_year;
    private String rating;
    private String duration;
    private String listed_in;
    private String description;

    public Movie () { } 

    public Movie(int show_id, String type, String title, String director, String[] cast, String country, String date_added, int release_year, String rating, String duration, String listed_in, String description) {
        setId(show_id);
        setType(type);
        setTitle(title);
        setDirector(director);
        setCast(cast);
        setCountry(country);
        setDateAdded(date_added);
        setReleaseYear(release_year);
        setRating(rating);
        setDuration(duration);
        setListedIn(listed_in);
        setDescription(description);
    }

    public int getId() {
        return show_id;
    }

    public void setId(int show_id) {
        this.show_id = show_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String[] getCast() {
        return cast;
    }

    public void setCast(String[] cast) {
        this.cast = cast;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDateAdded() {
        return date_added;
    }

    public void setDateAdded(String date_added) {
        this.date_added = date_added;
    }

    public int getReleaseYear() {
        return release_year;
    }

    public void setReleaseYear(int release_year) {
        this.release_year = release_year;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getListedIn() {
        return listed_in;
    }

    public void setListedIn(String listed_in) {
        this.listed_in = listed_in;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return "\nMovie{" +
                "show_id=" + show_id +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", director='" + director + '\'' +
                ", cast=" + Arrays.toString(cast) +
                ", country='" + country + '\'' +
                ", date_added='" + date_added + '\'' +
                ", release_year=" + release_year +
                ", rating='" + rating + '\'' +
                ", duration='" + duration + '\'' +
                ", listed_in='" + listed_in + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    // Gravando os dados dos filmes em um arquivo binário
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(show_id);
        dos.writeUTF(type);
        dos.writeUTF(title);
        dos.writeUTF(director);
        for (String c : cast) {
            byte[] cBytes = c.getBytes("UTF-8");
            dos.writeInt(cBytes.length);
            dos.writeUTF(c);
        }
        dos.writeUTF(country);
        dos.writeUTF(date_added);
        dos.writeInt(release_year);
        dos.writeUTF(rating);
        dos.writeUTF(duration);
        dos.writeUTF(listed_in);
        dos.writeUTF(description);
        return baos.toByteArray();
    }

    //Lendo os dados dos filmes de um arquivo binário
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);
        show_id = dis.readInt();
        type = dis.readUTF();
        title = dis.readUTF();
        director = dis.readUTF();
        int castSize = dis.readInt();
        cast = new String[castSize];
        for (int i = 0; i < castSize; i++) {
            cast[i] = dis.readUTF();
        }
        country = dis.readUTF();
        date_added = dis.readUTF();
        release_year = dis.readInt();
        rating = dis.readUTF();
        duration = dis.readUTF();
        listed_in = dis.readUTF();
        description = dis.readUTF();
    }

}


