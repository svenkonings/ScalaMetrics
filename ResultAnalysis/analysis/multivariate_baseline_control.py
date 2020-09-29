import warnings
from random import Random

import pandas as pd
from sklearn.exceptions import ConvergenceWarning
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import StratifiedKFold, cross_val_predict

from analysis import categories, projects, get_metric_results, get_columns, to_binary, get_stats, \
    parse_args, save_dataframe
from analysis.summarise import read_all, summarise

random = Random(42)


def main(args):
    warnings.filterwarnings("ignore", category=RuntimeWarning, module="sklearn")
    warnings.filterwarnings("ignore", category=ConvergenceWarning, module="sklearn")
    folder = f'{args.folder}/regression/mulitvariate-baseline-control/'
    estimator = LogisticRegression(class_weight='balanced', random_state=42)
    cv = StratifiedKFold(n_splits=10, shuffle=True, random_state=42)
    for category in categories:
        for path, name in projects.items():
            df = get_metric_results('baseline', path, category)
            if df is not None:
                multivariate_test(df, folder, category, name, estimator, cv, args)


def multivariate_test(df, folder, category, name, estimator, cv, args):
    print(f'[{category}] Mulitvariate baseline: {name}')
    faults = df['faults'].apply(to_binary)
    result = pd.DataFrame(
        columns=['name', 'tn', 'fp', 'fn', 'tp', 'r2', 'precision', 'recall', 'mcc']
    )

    def flip_random(value, probability=10):
        if random.randint(1, 100) <= probability:
            if value == 0:
                return 1
            else:
                return 0
        else:
            return value

    flipped_faults = faults.apply(flip_random)
    univariate_prediction = cross_val_predict(estimator, flipped_faults.values.reshape(-1, 1), faults, cv=cv)
    univariate_result = get_stats(faults, univariate_prediction)
    univariate_result['name'] = 'univariate'
    result = result.append(univariate_result, ignore_index=True)

    baseline_columns = get_columns(df, args)
    baseline_data = df[baseline_columns]
    baseline_prediction = cross_val_predict(estimator, baseline_data, faults, cv=cv)
    baseline_result = get_stats(faults, baseline_prediction)
    baseline_result['name'] = 'baseline'
    result = result.append(baseline_result, ignore_index=True)

    control_data = baseline_data.join(flipped_faults)
    control_prediction = cross_val_predict(estimator, control_data, faults, cv=cv)
    control_result = get_stats(faults, control_prediction)
    control_result['name'] = 'control'
    result = result.append(control_result, ignore_index=True)
    save_dataframe(result, folder + category, name, False)


def summarise_multivariate(args):
    folder = f'{args.folder}/regression/mulitvariate-baseline-control/'
    for category in categories:
        path = folder + category
        df = read_all(path, ['name', 'precision', 'recall', 'mcc'])
        if df is not None:
            summarise(df, path, category)


if __name__ == '__main__':
    args = parse_args()
    main(args)
    summarise_multivariate(args)
