import http.server
import socketserver
import os
import sys

# 配置
PORT = 9001
HOST = "0.0.0.0"
DIRECTORY = r"./"  # 修改为你的目录路径

class SilentHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    """静默的HTTP请求处理器，完全禁止输出"""
    def log_message(self, format, *args):
        # 完全禁止访问日志输出
        pass

def start_server():
    # 确保完全静默运行，重定向所有输出
    if hasattr(sys.stdout, 'close'):
        sys.stdout.close()
    if hasattr(sys.stderr, 'close'):
        sys.stderr.close()
    
    # 重新指向空设备
    sys.stdout = open(os.devnull, 'w')
    sys.stderr = open(os.devnull, 'w')
    
    # 切换到指定目录（静默）
    if os.path.exists(DIRECTORY):
        os.chdir(DIRECTORY)
    
    # 创建静默服务器
    Handler = SilentHTTPRequestHandler
    
    try:
        with socketserver.TCPServer((HOST, PORT), Handler) as httpd:
            # 设置服务器为静默模式
            httpd.allow_reuse_address = True
            httpd.serve_forever()
    except:
        # 静默处理所有异常
        pass

if __name__ == "__main__":
    start_server()