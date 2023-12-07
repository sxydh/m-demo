# 类的声明
class Man:
    # 类的变量
    name = None
    age = None

    # 类的方法
    # 方法上的self需要显示声明
    # 方法内部只能通过self访问类的变量，Python没有Java的关键字this。
    # 方法不能重载
    def introduce(self):
        print(f'name = {self.name}, age = {self.age}')

    def say(self, msg):
        print(f'{self.name} say: {msg}')


if __name__ == '__main__':
    man = Man()
    man.name = 'Jack'
    man.age = 20
    man.introduce()
    man.say('Hello World!')
