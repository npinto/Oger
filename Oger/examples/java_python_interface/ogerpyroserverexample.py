# To change this template, choose Tools | Templates
# and open the template in the editor.

__author__="uli"
__date__ ="$29.09.2010 14:11:48$"

import Oger
import Pyro.core
import pylab
import mdp
import numpy

def numpy_array_to_native_array(na):
    retval = []
    h, w = na.shape
    for y in range(h):
        line = []
        for x in range(w):
            line.append(float(na[y,x]))
        retval.append(line)
    return retval

def natna(na):
    return numpy_array_to_native_array(na)

def list_of_numpy_arrays_to_list_of_native_array(numpy_array_list):
    retval = []
    for na in numpy_array_list:
        retval.append(natna(na))
    return retval

def lonatlona(lona):
    return list_of_numpy_arrays_to_list_of_native_array(lona)

class OgerAccess(Pyro.core.ObjBase):
    def __init__(self):
        Pyro.core.ObjBase.__init__(self)

    def get_narma_data(self, sample_len=1000):
        [x, y] = Oger.datasets.narma30(sample_len=1000)
        return [lonatlona(x), lonatlona(y)]

    def pylab_plot(self, x, y):
        pylab.plot(x, y)
        pylab.show()

    def make_narma_flow(self, inputs):
        reservoir = Oger.nodes.ReservoirNode(inputs, 100, input_scaling=0.05)
        readout = Oger.nodes.RidgeRegressionNode()
        # build network with MDP framework
        self.flow = mdp.Flow([reservoir, readout])

    def train_narma_flow(self, xx, yy):

#        [x, y] = Oger.datasets.narma30(sample_len=1000)

        x = [numpy.array(m) for m in xx]
        y = [numpy.array(m) for m in yy]

        data = [x[0:-1], zip(x[0:-1], y[0:-1])]
        # train the flow
        self.flow.train(data)
        # apply the trained flow to the training data and test data

def main():
    Pyro.core.initServer()
    daemon = Pyro.core.Daemon()
    uri = daemon.connect(OgerAccess(), "oger_access")
    print "port: ", daemon.port
    print "uri:  ", uri
    daemon.requestLoop()

if __name__ == "__main__":
    main()
