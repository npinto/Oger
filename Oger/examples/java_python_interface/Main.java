/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package exampleogerclient;

import java.util.ArrayList;
import java.util.Properties;
import org.python.core.PyArray;
import org.python.core.PyFloat;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 *
 * @author uli
 */
public class Main {

    // jython_site_packages_path is the path you have Pyro installed in
    public static PythonInterpreter preparePythonInterpreter(String pyro_path) {
        System.out.println("initializing oger access via jython/pyro");
        Properties p = System.getProperties();
        p.put("python.path", pyro_path);
        PythonInterpreter.initialize(p, null, new String[]{});
        PythonInterpreter interp = new PythonInterpreter();
        interp.exec("import Pyro.core");
        interp.exec("oger_access = Pyro.core.getProxyForURI(\"PYROLOC://localhost:7766/oger_access\")");
        System.out.println("oger access initialized");
        return interp;
    }

    public static void testPyLabPlotting(PythonInterpreter interp) {
        PyArray xx = new PyArray(Double.class, 0);
        PyArray yy = new PyArray(Double.class, 0);
        for (double d = 0; d < 100.0; ++d) {
            xx.append(new PyFloat(d * d));
            yy.append(new PyFloat(d * d * d));
        }
        interp.set("xx", xx);
        interp.set("yy", yy);
        interp.exec("oger_access.pylab_plot([float(d) for d in xx], [float(d) for d in yy])");
    }

    public static double[][] pyObjToDblMat(PyObject po) {
        ArrayList<double[]> retval = new ArrayList<double[]>();
        for (PyObject poo : po.asIterable()) {
            ArrayList<Double> ald = new ArrayList<Double>();
            for (PyObject pooo : poo.asIterable()) {
                ald.add(pooo.asDouble());
            }
            double[] arr = new double[ald.size()];
            for (int i = 0;i<ald.size();++i) {
                arr[i] = ald.get(i);
            }
            retval.add(arr);
        }
        return retval.toArray(new double[0][]);
    }

    public static PyArray javaDblMatToPyArray(double[][] mat) {
        PyArray retval = new PyArray(PyArray.class, 0);
        for (double[] l : mat) {
            PyArray pa = new PyArray(Double.class, 0);
            for (double d : l) {
                pa.append(new PyFloat(d));
            }
            retval.append(pa);
        }
        return retval;
    }

    public static PyArray javaArrayListOfDblMatToPyArray(ArrayList<double[][]> alm) {
        PyArray retval = new PyArray(PyArray.class, 0);
        for (double[][] m : alm) {
            retval.append(javaDblMatToPyArray(m));
        }
        return retval;
    }

    public static void testNarmaExample(PythonInterpreter interp) {

        System.out.println("fetch narma data from oger and convert it into java native types");
        interp.exec("narma_data = oger_access.get_narma_data()");
//        interp.exec("print \"len(narma_data):\" + str(len(narma_data))");
//        interp.exec("print \"len(narma_data[0]):\" + str(len(narma_data[0]))");
//        interp.exec("print \"len(narma_data[1]):\" + str(len(narma_data[1]))");
//        interp.exec("print \"len(narma_data[0][0]):\" + str(len(narma_data[0][0]))");
//        interp.exec("print \"len(narma_data[1][0]):\" + str(len(narma_data[1][0]))");
//        interp.exec("print \"len(narma_data[0][0][0]):\" + str(len(narma_data[0][0][0]))");
//        interp.exec("print \"len(narma_data[1][0][0]):\" + str(len(narma_data[1][0][0]))");
        PyObject narma_data = interp.get("narma_data");
        ArrayList<PyObject> alpo = new ArrayList<PyObject>();
        for (PyObject po : narma_data.asIterable()) {
            alpo.add(po);
        }
//        System.out.println("alpo_size: " + alpo.size());
        // x
        ArrayList<double[][]> x = new ArrayList<double[][]>();
        for (PyObject po : alpo.get(0).asIterable()) {
            x.add(pyObjToDblMat(po));
        }
//        System.out.println("x.size(): " + x.size());
//        System.out.println("x.get(0).length: " + x.get(0).length);
//        System.out.println("x.get(0)[0].length: " + x.get(0)[0].length);
        // y
        ArrayList<double[][]> y = new ArrayList<double[][]>();
        for (PyObject po : alpo.get(1).asIterable()) {
            y.add(pyObjToDblMat(po));
        }
//        System.out.println("y.size(): " + y.size());
//        System.out.println("y.get(0).length: " + y.get(0).length);
//        System.out.println("y.get(0)[0].length: " + y.get(0)[0].length);

        System.out.println("make narma flow");
        interp.set("inputs", 1);
        interp.exec("oger_access.make_narma_flow(inputs)");

        System.out.println("call narma flow training with the java native data");
        PyArray xpa = javaArrayListOfDblMatToPyArray(x);

        PyArray ypa = javaArrayListOfDblMatToPyArray(y);

        interp.set("xpa", xpa);
        interp.set("ypa", ypa);

        interp.exec("xx = [[[v for v in l] for l in m] for m in xpa]");
        interp.exec("yy = [[[v for v in l] for l in m] for m in ypa]");

//        interp.exec("print narma_data[0]");
//        interp.exec("print xx");
//        interp.exec("print narma_data[1]");
//        interp.exec("print yy");

        interp.exec("oger_access.train_narma_flow(xx, yy)");
        System.out.println("success :-)");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PythonInterpreter interp = preparePythonInterpreter("/home/uli/netbeans-6.9.1/python/jython-2.5.1/Lib/site-packages");

        // pylab plot test
        // testPyLabPlotting(interp);

        // oger narma test
        testNarmaExample(interp);

    }
}
