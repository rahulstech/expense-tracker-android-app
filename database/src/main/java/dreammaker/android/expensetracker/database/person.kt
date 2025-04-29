package dreammaker.android.expensetracker.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Objects

@Entity(tableName = "persons")
open class Person(
    @SerializedName("_id")
    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "_id",)
    var personId: Long,
    @SerializedName("person_name")
    @ColumnInfo(name = "person_name",)
    var personName: String,
    open var due: Float
): Cloneable {
    @Deprecated("")
    @Ignore
    constructor(personId: Long, personName: String) : this(personId, personName, 0f)

    @Ignore
    @Deprecated("")
    constructor(personName: String) : this(0L, personName, 0f)

    @Ignore
    constructor() : this(0, "", 0f)


    override fun equals(other: Any?): Boolean {
        if (other == this) return true
        if (other is Person) {
            return other.personId == personId && other.personName == personName && other.due == due
        }
        return false;
    }

    override fun hashCode(): Int = Objects.hash(personId,personName,due)

    @Deprecated("", ReplaceWith("equals(p)"))
    fun equalContent(p: Person?): Boolean = equals(p);

    override fun toString(): String {
        return "Person{" +
                "personId=" + personId +
                ", personName='" + personName + '\'' +
                ", due=" + due +
                '}'
    }

    public override fun clone(): Person = Person(personId, personName, due)
}