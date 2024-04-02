package entities;

import jakarta.persistence.*;

@Entity
@Table(name="Categories")
@NamedQuery(name="Category.findCategoryById", query="SELECT a FROM CategoryEntity a WHERE a.id = :id")
@NamedQuery(name="Category.findCategoryByName", query="SELECT a FROM CategoryEntity a WHERE a.name = :name")
@NamedQuery(name="Category.findCategoryByCreator", query="SELECT a FROM CategoryEntity a WHERE a.creator = :creator")
@NamedQuery(name="Category.findCreatorByName", query="SELECT a FROM CategoryEntity a WHERE a.name = :name")
@NamedQuery(name="Category.findAll", query="SELECT a FROM CategoryEntity a")
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false, unique = true, updatable = false)
    private int id;
    @Column(name="name", nullable = false, unique = true)
    private String name;
    @Column(name="creator", nullable = false, unique = false)
    private String creator;




    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
