package com.example.overtimemanagesys_zan

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.overtimemanagesys_zan.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // 現在のFragmentを取得
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as? NavHostFragment
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()
        
        // FirstFragment以外ではメニューを非表示
        val isFirstFragment = currentFragment is FirstFragment
        val isSortMode = isFirstFragment && currentFragment is FirstFragment && currentFragment.isSortMode()
        
        menu.findItem(R.id.action_calendar)?.isVisible = isFirstFragment
        menu.findItem(R.id.action_calendar)?.isEnabled = !isSortMode
        menu.findItem(R.id.action_add_employee)?.isVisible = isFirstFragment
        menu.findItem(R.id.action_add_employee)?.isEnabled = !isSortMode
        menu.findItem(R.id.action_hidden_employees)?.isVisible = isFirstFragment
        menu.findItem(R.id.action_hidden_employees)?.isEnabled = !isSortMode
        val sortMenuItem = menu.findItem(R.id.action_sort)
        sortMenuItem?.isVisible = isFirstFragment
        if (isFirstFragment && currentFragment is FirstFragment) {
            // 並び替えモードに応じてメニュータイトルを変更
            sortMenuItem?.title = if (isSortMode) "完了" else "並び替え"
        }
        
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return when (item.itemId) {
            R.id.action_calendar -> {
                navController.navigate(R.id.action_FirstFragment_to_DateSelectCalendarFragment)
                true
            }
            R.id.action_add_employee -> {
                navController.navigate(R.id.action_FirstFragment_to_AddEmployeeFragment)
                true
            }
            R.id.action_hidden_employees -> {
                navController.navigate(R.id.action_FirstFragment_to_HiddenEmployeesFragment)
                true
            }
            R.id.action_sort -> {
                // FirstFragmentの並び替えモードを切り替え
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as? NavHostFragment
                val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()
                if (currentFragment is FirstFragment) {
                    if (currentFragment.isSortMode()) {
                        currentFragment.saveSortOrder()
                    } else {
                        currentFragment.toggleSortMode()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
