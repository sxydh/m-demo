from m_pyutil.msqlite import select
from pyecharts import options
from pyecharts.charts import Line, Bar

from src.main.ssq._ssq_app import DB_FILE


class YaxisData:

    def __init__(self, name, data):
        self.name = name
        self.data = data


def get_data() -> tuple:
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
    rows = select(sql='select distinct d, r, r2, r3, r4, r5, r6, b, n from t_ssq order by id desc limit 30',
                  f=DB_FILE)
    for row in rows:
        xaxis_data.append(row[0])
        bar_data.data.append(row[8])
        line_datas[0].data.append(row[1])
        line_datas[1].data.append(row[2])
        line_datas[2].data.append(row[3])
        line_datas[3].data.append(row[4])
        line_datas[4].data.append(row[5])
        line_datas[5].data.append(row[6])
        line_datas[6].data.append(row[7])
    return xaxis_data, bar_data, line_datas


def render(xaxis_data: list,
           bar_data: YaxisData,
           line_datas: list[YaxisData]):
    bar = Bar(init_opts=options.InitOpts(width='4000px', height='100vh'))
    bar.add_xaxis(xaxis_data)
    bar.add_yaxis(
        series_name=bar_data.name,
        y_axis=bar_data.data,
        bar_width='30%',
        itemstyle_opts=options.ItemStyleOpts(opacity=0.2))
    bar.set_global_opts(
        yaxis_opts=options.AxisOpts(interval=1))

    line = Line(init_opts=options.InitOpts(width='4000px', height='100vh'))
    line.add_xaxis(xaxis_data)
    for line_data in line_datas:
        line.add_yaxis(
            series_name=line_data.name,
            y_axis=line_data.data)
    line.set_global_opts(
        yaxis_opts=options.AxisOpts(interval=1))

    bar.overlap(line)
    bar.render('tmp/ssq_echarts.html')


def start():
    xaxis_data, bar_data, line_datas = get_data()
    render(xaxis_data, bar_data, line_datas)


if __name__ == '__main__':
    start()
