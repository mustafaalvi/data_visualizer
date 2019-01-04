package algorithm;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class DataSetTest {

    @Test
    public void addSingleInstance() {
        DataSet d = new DataSet();
        try {
            File f = new File(DataSetTest.class.getResource("DatasetSingleValidLineTester").toURI());
            d.fromTSDFile(f.toPath());
        }catch(Exception e){e.printStackTrace();}
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void addSingleInvalidInstance(){
        DataSet d = new DataSet();
        try {
            File f = new File(DataSetTest.class.getResource("DatasetSingleInvalidLineTester").toURI());
            d.fromTSDFile(f.toPath());
        }catch(URISyntaxException e) {
            e.printStackTrace();
        }catch(IOException f){
            f.printStackTrace();
        }
    }

    @Test(expected = DataSet.InvalidDataNameException.class)
    public void addSingleInvalidNameInstance() throws DataSet.InvalidDataNameException{
        DataSet d = new DataSet();
        try {
            File f = new File(DataSetTest.class.getResource("DatasetSingleInvalidNameTester").toURI());
            d.fromTSDFile(f.toPath());
        }catch(URISyntaxException e) {
            e.printStackTrace();
        }catch(IOException f){
            f.printStackTrace();
        }
    }


}