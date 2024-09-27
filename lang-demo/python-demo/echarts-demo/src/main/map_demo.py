from pyecharts import options as opts
from pyecharts.charts import Map
from pyecharts.faker import Faker

if __name__ == '__main__':
    # 如果代码分行，需要用括号圈起来。
    (Map()
     .add(series_name='商家A',
          data_pair=[list(z) for z in zip(Faker.provinces, Faker.values())],
          maptype='china')
     .set_global_opts(title_opts=opts.TitleOpts(title='Map-基本示例'))
     .render('tmp/map_demo.html'))
