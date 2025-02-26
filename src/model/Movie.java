package model;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Movie implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String DATE_FORMAT = "MMMM d, yyyy";
    private static final Locale DATE_LOCALE = Locale.US;

    private int show_id;
    private String type;
    private String title;
    private String director;
    private String[] cast;
    private String country;
    private Date date_added;
    private Integer release_year;
    private String rating;
    private String duration;
    private String listed_in;
    private String description;

    public Movie(int show_id) {
        this.show_id = show_id;
    }

    public Movie(int show_id, String type, String title, String director, String[] cast, String country, String date_added, Integer release_year, String rating, String duration, String listed_in, String description) {
        this.show_id = show_id;
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
        return cast != null ? cast.clone() : new String[0];
    }

    public void setCast(String[] cast) {
        this.cast = cast != null ? cast.clone() : null;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDateAdded() {
        if (date_added == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, DATE_LOCALE);
        return formatter.format(date_added);
    }

    public void setDateAdded(String date_added) {
        if (date_added == null || date_added.isEmpty()) {
            this.date_added = null;
            return;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, DATE_LOCALE);
            this.date_added = formatter.parse(date_added);
        } catch (ParseException e) {
            System.err.println("Erro ao converter a data: " + e.getMessage());
            this.date_added = null;
        }
    }

    public Integer getReleaseYear() {
        return release_year;
    }

    public void setReleaseYear(Integer release_year) {
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

    @Override
    public String toString() {
        return "Movie{" +
               "show_id=" + show_id +
               ", type='" + (type != null ? type : "") + '\'' +
               ", title='" + (title != null ? title : "") + '\'' +
               ", director='" + (director != null ? director : "") + '\'' +
               ", cast=" + (cast != null ? Arrays.toString(cast) : "[]") +
               ", country='" + (country != null ? country : "") + '\'' +
               ", date_added='" + getDateAdded() + '\'' +
               ", release_year=" + (release_year != null ? release_year : "") +
               ", rating='" + (rating != null ? rating : "") + '\'' +
               ", duration='" + (duration != null ? duration : "") + '\'' +
               ", listed_in='" + (listed_in != null ? listed_in : "") + '\'' +
               ", description='" + (description != null ? description : "") + '\'' +
               '}';
    }

    // Gravando os dados dos filmes em um arquivo binário
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(show_id);
        dos.writeUTF(type != null ? type : "");
        dos.writeUTF(title != null ? title : "");
        dos.writeUTF(director != null ? director : "");
        dos.writeInt(cast != null ? cast.length : 0);
        if (cast != null) {
            for (String c : cast) {
                dos.writeUTF(c != null ? c : "");
            }
        }
        dos.writeUTF(country != null ? country : "");
        dos.writeLong(date_added != null ? date_added.getTime() : -1);
        dos.writeInt(release_year != null ? release_year : -1);
        dos.writeUTF(rating != null ? rating : "");
        dos.writeUTF(duration != null ? duration : "");
        dos.writeUTF(listed_in != null ? listed_in : "");
        dos.writeUTF(description != null ? description : "");
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
        long dateTime = dis.readLong();
        date_added = dateTime != -1 ? new Date(dateTime) : null;
        int year = dis.readInt();
        release_year = year != -1 ? year : null;
        rating = dis.readUTF();
        duration = dis.readUTF();
        listed_in = dis.readUTF();
        description = dis.readUTF();
    }
}
