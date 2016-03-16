
import java.util.ArrayList;
import java.util.Comparator;

public class ArrayIndexComparator implements Comparator<Integer>
{
    private final ArrayList<Double> array;

    public ArrayIndexComparator(ArrayList<Double> array)
    {
        this.array = array;
    }
    public Integer[] createIndexArray() {
        Integer[] indexes = new Integer[array.size()];
        for (int i = 0; i < array.size(); i++) {
                indexes[i] = i; // Autoboxing
        }
        return indexes;
}
    @Override
    public int compare(Integer index1, Integer index2)
    {
        return Double.compare(array.get(index2),array.get(index1));
    }
}