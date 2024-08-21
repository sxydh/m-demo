from random import randint

from pyecharts.charts import Line
from pyecharts.options import LineItem

if __name__ == '__main__':
    line = Line()
    line.add_xaxis(['一月', '二月', '三月'])
    line.add_yaxis('消费', [
        LineItem(name='一月', value=randint(100, 160)),
        LineItem(name='二月', value=randint(100, 160)),
        LineItem(name='三月', value=randint(100, 160))])
    line.render('tmp/line_demo.html')
