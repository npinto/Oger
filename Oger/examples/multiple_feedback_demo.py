import Oger
import pylab
import mdp


if __name__ == "__main__":
    '''This demonstrates the use of feeding back two different values using the same feedback node:
    
            |--------------------------------------|
            ->   _____________    ->  Readout 1 -->|------|
                | Reservoir  | --|                        |--> Readout 3
            ->  -------------     ->  Readout 2 -->|------|
            |--------------------------------------|
            
        The readouts are trained using different values of the regularization constant, so they yield 
        different outputs, but are still fed back into the same reservoir. Their outputs are then fed to a 
        third ridge regression node. 
        This example is perhaps not very practical, but it shows how more complex feedback schemes can be built.
    '''
    freerun_steps = 500

    N = 100;
    x = Oger.datasets.mackey_glass(sample_len=1000)

    # construct individual nodes
    reservoir = Oger.nodes.LeakyReservoirNode(output_dim=N, input_dim=1, input_scaling=1, reset_states=False)
    reservoir.leak_rate = 0.5

    readout = Oger.nodes.RidgeRegressionNode(.0001, input_dim=N, output_dim=1)
    readout2 = Oger.nodes.RidgeRegressionNode(0.00001, input_dim=N, output_dim=1)

    output_layers = mdp.hinet.SameInputLayer([readout, readout2])

    readout3 = Oger.nodes.RidgeRegressionNode(0.01, input_dim=2)

    flow = Oger.nodes.FreerunFlow([reservoir, output_layers, readout3], freerun_steps=freerun_steps, verbose=1)

    flow._states = [[] for _ in range(len(flow.flow))]
    Oger.utils.mix_in(Oger.nodes.FreerunFlow, Oger.nodes.InspectableFlow)
    # Train the reservoir to do one-step ahead prediction using the teacher-forced signal
    flow.train([[], x, x])

    # build network with MDP framework
    # Save the states of the reservoir during training for later plotting

    z = flow.inspect(output_layers)
    y = flow.execute(x[0][0])
#    training_states = flow.inspect(reservoir)
#    training_output = flow.inspect(output_layers)

    pylab.figure()
    #pylab.plot(y)


    #pylab.subplot(313)
    pylab.plot(x[0][0][-2 * freerun_steps:])
    pylab.plot(y[-2 * freerun_steps:, :])
    pylab.plot(z[0][-2 * freerun_steps:, 0])
    pylab.plot(z[0][-2 * freerun_steps:, 1])

    pylab.subplot(212)
    pylab.plot(training_states)
    pylab.show()
