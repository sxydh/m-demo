from pyecharts.charts import Line

if __name__ == '__main__':
    line = Line()
    line.add_xaxis(['一月', '二月', '三月'])
    line.add_yaxis('消费', [12321, 11232, 12321])
    line.render()
