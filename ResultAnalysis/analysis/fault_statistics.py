import pandas as pd

from analysis import categories, projects, save_dataframe, get_metric_results, parse_args


def main(args):
    folder = f'{args.folder}/regression/fault-statistics/'
    for category in categories:
        statistics = pd.DataFrame(
            columns=['name', 'rows', 'faulty_rows', 'non_faulty_rows', 'percentage_faulty']
        )
        for path, name in projects.items():
            df = get_metric_results(args.folder, path, category)
            if df is not None:
                statistics = fault_statistics(df, statistics, category, name)
        if not statistics.empty:
            save_dataframe(statistics, folder, category, False)


def fault_statistics(df, statistics, category, name):
    print(f'[{category}] Fault-statistics: {name}')
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
    return statistics.append(result, ignore_index=True)


if __name__ == '__main__':
    main(parse_args())
