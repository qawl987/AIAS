# %%
import onnx

onnx_model = onnx.load('./mobilenetv2-10.onnx')

# The model is represented as a protobuf structure and it can be accessed
# using the standard python-for-protobuf methods

## list all the operator types in the model
node_list = []
count = []
for i in onnx_model.graph.node:
    if (i.op_type not in node_list):
        node_list.append(i.op_type)
        count.append(1)
    else:
        idx = node_list.index(i.op_type)
        count[idx] = count[idx]+1
print(node_list)
print(count)


# %%
# print(onnx_model.graph)

# %%
import onnx

onnx_model = onnx.load('./mobilenetv2-10.onnx')
## need to run shape inference in order to get a full value_info list
onnx_model = onnx.shape_inference.infer_shapes(onnx_model)

## List all tensor names in the graph
input_nlist = [k.name for k in onnx_model.graph.input]
value_info_nlist = [k.name for k in onnx_model.graph.value_info]

print('\ninput list: {}'.format(input_nlist))
print('\nvalue_info list: {}'.format(value_info_nlist))

## a simple function to calculate the tensor size and extract dimension information
def get_size(shape):
    dims = []
    ndim = len(shape.dim)
    size = 1
    for i in range(ndim):
        size = size * shape.dim[i].dim_value
        dims.append(shape.dim[i].dim_value)
    return dims, size

## find all `Conv` operators and print its input information
for i in onnx_model.graph.node:
    if (i.op_type == 'Conv'):
        print('\n-- Conv "{}" --'.format(i.name))
        for attr in i.attribute:
            if attr.name == 'kernel_shape':
                kernel_size = attr.ints  # Kernel size (height, width)
            elif attr.name == 'strides':
                stride = attr.ints  # Stride size (height, width)
            elif attr.name == 'dilations':
                dilation = attr.ints  # Dilation (height, width)
        print(f"Kernel size: {kernel_size}, Stride: {stride}, Dilation: {dilation}")
        for j in i.input:
            if j in input_nlist:
                idx = input_nlist.index(j)
                (dims, size) = get_size(onnx_model.graph.input[idx].type.tensor_type.shape)
                print('input {} has {} elements dims = {}'.format(j, size, dims  ))
            elif j in value_info_nlist:
                idx = value_info_nlist.index(j)
                (dims, size) = get_size(onnx_model.graph.value_info[idx].type.tensor_type.shape)
                print('input {} has {} elements dims = {}'.format(j, size, dims))

# %%
import onnx

# Load the ONNX model
onnx_model = onnx.load('./mobilenetv2-10.onnx')

# Iterate through all the nodes in the graph
for node in onnx_model.graph.node:
    # Check if the node is a convolution (Conv) layer
    if node.op_type == 'Conv':
        print(f"Conv2D Layer: {node.name}")
        
        # Initialize variables for width, height, channels, dilation, stride, and kernel size
        kernel_size = None
        stride = None
        dilation = None

        # Go through the attributes of the convolution node
        for attr in node.attribute:
            if attr.name == 'kernel_shape':
                kernel_size = attr.ints  # Kernel size (height, width)
            elif attr.name == 'strides':
                stride = attr.ints  # Stride size (height, width)
            elif attr.name == 'dilations':
                dilation = attr.ints  # Dilation (height, width)
        
        # Print the layer details
        print(f"  Kernel size: {kernel_size}")
        print(f"  Stride: {stride}")
        print(f"  Dilation: {dilation}")


