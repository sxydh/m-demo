from pyecharts import options
from pyecharts.charts import Line, Bar

from src.main.util.common import get_sqlite_connection


class YaxisData:
    def __init__(self, name, data):
        self.name = name
        self.data = data


def get_data():
    with get_sqlite_connection() as conn:
        cursor = conn.cursor()
        cursor.execute('select distinct n1, n2, n3, n4, n5, n6, n7, id, p from t_ssq order by id desc limit 30')
        xaxis_data = []
        bar_data = YaxisData('注', [])
        line_datas = [
            YaxisData('红1', []),
            YaxisData('红2', []),
            YaxisData('红3', []),
            YaxisData('红4', []),
            YaxisData('红5', []),
            YaxisData('红6', []),
            YaxisData('蓝1', []),
        ]
        for r in cursor.fetchall():
            xaxis_data.append(r[7])
            bar_data.data.append(r[8])
            line_datas[0].data.append(r[0])
            line_datas[1].data.append(r[1])
            line_datas[2].data.append(r[2])
            line_datas[3].data.append(r[3])
            line_datas[4].data.append(r[4])
            line_datas[5].data.append(r[5])
            line_datas[6].data.append(r[6])
        return [xaxis_data, bar_data, line_datas]


def render(xaxis_data, bar_data: YaxisData, line_datas: list[YaxisData]):
    bar = Bar(init_opts=options.InitOpts(width='4000px', height='100vh'))
    bar.add_xaxis(xaxis_data)
    bar.add_yaxis(
        series_name=bar_data.name,
        y_axis=bar_data.data,
        itemstyle_opts=options.ItemStyleOpts(opacity=0.2)
    )

    line = Line(init_opts=options.InitOpts(width='4000px', height='100vh'))
    line.add_xaxis(xaxis_data)
    for yaxis_data in line_datas:
        line.add_yaxis(
            series_name=yaxis_data.name,
            y_axis=yaxis_data.data)
    line.set_global_opts(
        yaxis_opts=options.AxisOpts(min_=1, split_number=33, max_=33)
    )

    bar.overlap(line)
    bar.render('tmp/ssq_echarts.html')


def start():
    xaxis_data, bar_data, line_datas = get_data()
    render(xaxis_data, bar_data, line_datas)


if __name__ == '__main__':
    start()
