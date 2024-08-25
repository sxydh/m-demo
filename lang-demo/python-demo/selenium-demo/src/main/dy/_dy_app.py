from selenium.webdriver.common.by import By

from src.main.util.cli import Cli


def login_close(cli: Cli):
    close = cli.find_element_d(by=By.CSS_SELECTOR, value='.douyin-login__close', timeout=0, count=1, raise_e=False)
    if close is not None:
        cli.click(close)


# noinspection PyBroadException
def start():
    cli = Cli(undetected=False,
              images_disabled=True,
              headless=False,
              extensions=['D:/Code/1-my/m-demo/ide-demo/chrome-demo/extension/blockurl-demo.crx'])
    cli.get('https://www.douyin.com/?recommend=1')

    while True:
        try:
            login_close(cli)
            infos = cli.find_elements_d(by=By.CSS_SELECTOR, value='.video-info-detail')
            players = cli.find_elements_d(by=By.CSS_SELECTOR, value='.immersive-player-switch-on-hide-interaction-area')
            info = None
            player = None
            if len(infos) == 1:
                info = infos[0]
                player = players[0]
            elif len(infos) == 3:
                info = infos[1]
                player = players[1]
            if info is not None:
                print(info.get_attribute('innerText'))
                print(player.get_attribute('innerText'))
                print()
        except Exception as _:
            pass

        try:
            login_close(cli)
            arrow = cli.find_element_d(by=By.CSS_SELECTOR, value='[data-e2e="video-switch-next-arrow"]', timeout=0, count=1, raise_e=False)
            cli.click(arrow)
        except Exception as _:
            pass


if __name__ == '__main__':
    start()
