import psycopg2
try:
    conn = psycopg2.connect(host='127.0.0.1', port=54321, user='system', password='ysb18316923897', dbname='kingbase')
    cur = conn.cursor()
    cur.execute('SET search_path TO biz_factor')
    
    cur.execute('SELECT COUNT(*) FROM ak_fund_profile')
    cnt = cur.fetchone()[0]
    print(f'ak_fund_profile records: {cnt}')
    
    cur.execute('SELECT COUNT(*) FROM ak_fund_company')
    cnt = cur.fetchone()[0]
    print(f'ak_fund_company records: {cnt}')
    
    cur.execute('SELECT COUNT(*) FROM ak_fund_nav')
    cnt = cur.fetchone()[0]
    print(f'ak_fund_nav records: {cnt}')
    
    cur.execute('SELECT COUNT(*) FROM base_factor_value')
    cnt = cur.fetchone()[0]
    print(f'base_factor_value records: {cnt}')
    
    print('\n--- Sample funds ---')
    cur.execute('SELECT fund_code, fund_name, fund_type FROM ak_fund_profile ORDER BY fund_code LIMIT 10')
    for r in cur.fetchall():
        print(f'  {r[0]} {r[1]} [{r[2]}]')
    
    cur.close()
    conn.close()
except Exception as e:
    print(f'Error: {e}')
