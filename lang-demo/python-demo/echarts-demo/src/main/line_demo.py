from random import randint

from pyecharts import options
from pyecharts.charts import Line
from pyecharts.options import LineItem

if __name__ == '__main__':
    line = Line(init_opts=options.InitOpts(width='100wh', height='100vh'))
    line.add_xaxis(['一月', '二月', '三月'])
    line.add_yaxis(
        series_name='消费',
        y_axis=[
            LineItem(name='一月', value=randint(0, 35)),
            LineItem(name='二月', value=randint(0, 35)),
            LineItem(name='三月', value=randint(0, 35))])
    line.set_global_opts(
        yaxis_opts=options.AxisOpts(min_=1, split_number=35, max_=35)
    )
    line.render('tmp/line_demo.html')
