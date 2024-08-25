import time

from selenium.webdriver.common.by import By

from src.main.util.cli import Cli


def login_close(cli: Cli):
    close = cli.find_element_d(by=By.CSS_SELECTOR, value='.douyin-login__close', timeout=0, count=1, raise_e=False)
    if close is not None:
        cli.click(close)


def start():
    cli = Cli(undetected=False,
              images_disabled=True,
              headless=False,
              extensions=['D:/Code/1-my/m-demo/ide-demo/chrome-demo/extension/blockurl-demo.crx'])
    cli.get('https://www.douyin.com/?recommend=1')

    while True:
        arrow = cli.find_element_d(by=By.CSS_SELECTOR, value='[data-e2e="video-switch-next-arrow"]')
        login_close(cli)
        cli.click(arrow)
        time.sleep(1)


if __name__ == '__main__':
    start()
