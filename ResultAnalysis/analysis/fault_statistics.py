import argparse

import pandas as pd

from analysis import categories, projects, save_dataframe, get_metric_results


def main(args):
    folder = f'{args.folder}/regression/fault-statistics/'
    for category in categories:
        fault_statistics = pd.DataFrame(
            columns=['name', 'rows', 'faulty_rows', 'non_faulty_rows', 'percentage_faulty']
        )
        for path, name in projects.items():
            print(f'[{category}] Fault-statistics: {name}')
            df = get_metric_results(args.folder, path, category)
            total_rows = len(df)
            faulty_rows = len(df[df['faults'] > 0])
            non_faulty_rows = len(df[df['faults'] == 0])
            percentage_faulty = (faulty_rows / total_rows) * 100
            result = {
                'name': name,
                'rows': total_rows,
                'faulty_rows': faulty_rows,
                'non_faulty_rows': non_faulty_rows,
                'percentage_faulty': percentage_faulty
            }
            fault_statistics = fault_statistics.append(result, ignore_index=True)
        save_dataframe(fault_statistics, folder, category, False)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--folder', help='Select folder to analyse', dest='folder', required=True)
    main(parser.parse_args())
